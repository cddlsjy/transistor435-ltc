#!/usr/bin/env python3
"""
打包本地项目目录并上传到 GitHub 仓库（覆盖）
用法：python upload_project.py <project_dir> <target_repo> <branch> [github_token]
"""

import os
import sys
import zipfile
import tempfile
import base64
import requests
from pathlib import Path

def upload_project(project_dir, target_repo, branch, token):
    # 1. 打包项目目录（排除常见无关目录）
    zip_path = os.path.join(tempfile.gettempdir(), f"{os.path.basename(project_dir)}.zip")
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zf:
        for root, dirs, files in os.walk(project_dir):
            dirs[:] = [d for d in dirs if d not in ('.git', '__pycache__', 'build', '.gradle', '.idea')]
            for file in files:
                full = os.path.join(root, file)
                arc = os.path.relpath(full, os.path.dirname(project_dir))
                zf.write(full, arc)

    # 2. 上传到 GitHub
    filename = os.path.basename(zip_path)
    url = f"https://api.github.com/repos/{target_repo}/contents/{filename}"
    headers = {"Authorization": f"token {token}"}
    with open(zip_path, 'rb') as f:
        content_b64 = base64.b64encode(f.read()).decode()

    # 检查是否已存在
    resp = requests.get(url, headers=headers, params={"ref": branch})
    sha = resp.json().get("sha") if resp.status_code == 200 else None

    data = {
        "message": f"Upload {filename}",
        "content": content_b64,
        "branch": branch
    }
    if sha:
        data["sha"] = sha
    resp = requests.put(url, headers=headers, json=data)
    resp.raise_for_status()
    os.unlink(zip_path)
    print(f"✅ 项目已打包并上传到 {target_repo}/{filename}")

if __name__ == "__main__":
    if len(sys.argv) >= 4:
        project_dir = sys.argv[1]
        target_repo = sys.argv[2]
        branch = sys.argv[3]
        token = sys.argv[4] if len(sys.argv) > 4 else os.environ.get("GITHUB_TOKEN")
    else:
        project_dir = os.environ.get("LOCAL_DIR")
        target_repo = os.environ.get("TARGET_REPO")
        branch = os.environ.get("BRANCH", "main")
        token = os.environ.get("GITHUB_TOKEN")
        if not (project_dir and target_repo and token):
            print("请提供参数或设置环境变量")
            sys.exit(1)
    upload_project(project_dir, target_repo, branch, token)