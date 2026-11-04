"""Boot a disposable Minecraft server, check its log, and stop it cleanly."""

import argparse
import queue
import subprocess
import threading
import time
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java", required=True)
    parser.add_argument("--jar", required=True)
    parser.add_argument("--directory", required=True)
    parser.add_argument("--marker", required=True)
    parser.add_argument("--timeout", type=int, default=180)
    args = parser.parse_args()
    directory = Path(args.directory).resolve()
    output = queue.Queue()
    process = subprocess.Popen(
        [args.java, "-Xms256M", "-Xmx1G", "-Dmixin.debug.verbose=true", "-jar",
         str(Path(args.jar).resolve()), "--nogui"],
        cwd=directory, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT, text=True, encoding="utf-8", errors="replace",
    )

    def collect():
        for line in process.stdout:
            output.put(line)
        output.put(None)

    threading.Thread(target=collect, daemon=True).start()
    lines = []
    ready = False
    deadline = time.monotonic() + args.timeout
    try:
        while time.monotonic() < deadline:
            try:
                line = output.get(timeout=1)
            except queue.Empty:
                continue
            if line is None:
                break
            print(line, end="", flush=True)
            lines.append(line)
            if not ready and 'Done (' in line:
                ready = True
                process.stdin.write("stop\n")
                process.stdin.flush()
        else:
            raise RuntimeError("Server did not finish the smoke check before the timeout.")
        result = process.wait(timeout=15)
        log = "".join(lines)
        if not ready or args.marker not in log or result != 0:
            raise RuntimeError(f"Smoke check failed: ready={ready}, exit={result}.")
        if any(error in log for error in ("Mixin apply failed", "Mixin transformation of",
                                         "Error loading class", "Error occurred while enabling")):
            raise RuntimeError("Server logged a mod/plugin loading failure.")
        print("Server smoke check passed: initialized, ready, and stopped cleanly.")
    finally:
        if process.poll() is None:
            process.kill()
            process.wait()
        (directory / "smoke-output.log").write_text("".join(lines), encoding="utf-8")


if __name__ == "__main__":
    main()
