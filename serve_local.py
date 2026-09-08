#!/usr/bin/env python3
import os
import sys
import json
import socket
import subprocess
from http.server import HTTPServer, SimpleHTTPRequestHandler

PORT = 8080
DIST_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "dist")

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        pass

    try:
        out = subprocess.check_output("ifconfig | grep 'inet ' | grep -v 127.0.0.1", shell=True).decode()
        for line in out.splitlines():
            parts = line.strip().split()
            if len(parts) >= 2:
                return parts[1]
    except Exception:
        pass

    return "127.0.0.1"

def setup_repo_files(local_ip):
    os.makedirs(DIST_DIR, exist_ok=True)
    base_url = f"http://{local_ip}:{PORT}"

    repo_data = {
        "name": "Layarkaca Kid Local Repo",
        "description": "Local repository for LK21 extension",
        "manifestVersion": 1,
        "pluginLists": [
            f"{base_url}/plugins.json"
        ]
    }

    plugins_data = [
        {
            "name": "Layarkaca Kid Dev",
            "internalName": "LayarkacaKidDev",
            "pluginClassName": "com.layarkacakid.dev.LayarkacaKidPlugin",
            "version": 1,
            "url": f"{base_url}/layarkaca-kid-dev.cs3",
            "apiVersion": 1,
            "authors": ["hmaehm"],
            "description": "LK21 movie and series extension with configurable domain settings",
            "types": ["Movie", "TvSeries"]
        }
    ]

    with open(os.path.join(DIST_DIR, "repo.json"), "w") as f:
        json.dump(repo_data, f, indent=2)

    with open(os.path.join(DIST_DIR, "plugins.json"), "w") as f:
        json.dump(plugins_data, f, indent=2)

class CORSRequestHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIST_DIR, **kwargs)

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "*")
        super().end_headers()

def main():
    local_ip = get_local_ip()
    setup_repo_files(local_ip)

    repo_url = f"http://{local_ip}:{PORT}/repo.json"
    cs3_path = os.path.join(DIST_DIR, "layarkaca-kid-dev.cs3")

    print("=" * 65)
    print("🚀 Cloudstream Local Repository Server")
    print("=" * 65)
    print(f"Local IP Address : {local_ip}")
    print(f"Serving folder   : {DIST_DIR}")
    print()
    print("In Cloudstream (Settings > Extensions > Add Repository):")
    print(f"👉 {repo_url}")
    print()
    if not os.path.exists(cs3_path):
        print("⚠️  Note: 'layarkaca-kid-dev.cs3' not found in dist/ yet.")
        print("   Once built, copy your .cs3 file into the dist/ directory:")
        print(f"   cp build/layarkaca-kid-dev.cs3 dist/")
    else:
        print("✅ Plugin file found: dist/layarkaca-kid-dev.cs3")
    print("=" * 65)
    print("Press Ctrl+C to stop the server.")

    httpd = HTTPServer(("0.0.0.0", PORT), CORSRequestHandler)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped.")

if __name__ == "__main__":
    main()
