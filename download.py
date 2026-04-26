#!/usr/bin/env python3
"""
从 GitHub 下载仓库 ZIP 并解压到本地目录（强制覆盖）
用法：python download_and_extract.py <repo> <branch> <target_dir> [github_token]
如果不提供参数，则从环境变量或配置文件读取
"""

import os
import sys
import zipfile
import shutil
import tempfile
import requests
from pathlib import Path

def download_and_extract(repo, branch, target_dir, token=None):
    # 1. 清空目标目录（强制覆盖，无确认）
    if os.path.exists(target_dir):
        shutil.rmtree(target_dir)
    os.makedirs(target_dir, exist_ok=True)

    # 2. 下载 ZIP
    url = f"https://api.github.com/repos/{repo}/zipball/{branch}"
    headers = {}
    if token:
        headers["Authorization"] = f"token {token}"
    resp = requests.get(url, headers=headers, stream=True)
    resp.raise_for_status()

    zip_path = os.path.join(tempfile.gettempdir(), "source.zip")
    with open(zip_path, 'wb') as f:
        for chunk in resp.iter_content(chunk_size=8192):
            f.write(chunk)

    # 3. 解压并处理可能的单层目录
    with zipfile.ZipFile(zip_path, 'r') as zf:
        top_dirs = set()
        for name in zf.namelist():
            top = name.split('/')[0]
            if top:
                top_dirs.add(top)
        if len(top_dirs) == 1:
            tmp = tempfile.mkdtemp()
            zf.extractall(tmp)
            src = os.path.join(tmp, list(top_dirs)[0])
            for item in os.listdir(src):
                shutil.move(os.path.join(src, item), target_dir)
            shutil.rmtree(tmp)
        else:
            zf.extractall(target_dir)
    os.unlink(zip_path)
    print(f"✅ 代码已下载并解压到 {target_dir}")

if __name__ == "__main__":
    # 参数顺序：repo branch target_dir [token]
    if len(sys.argv) >= 4:
        repo = sys.argv[1]
        branch = sys.argv[2]
        target_dir = sys.argv[3]
        token = sys.argv[4] if len(sys.argv) > 4 else os.environ.get("GITHUB_TOKEN")
    else:
        # 从环境变量读取
        repo = os.environ.get("SOURCE_REPO")
        branch = os.environ.get("BRANCH", "main")
        target_dir = os.environ.get("LOCAL_DIR")
        token = os.environ.get("GITHUB_TOKEN")
        if not (repo and target_dir):
            print("请提供参数: python download_and_extract.py <repo> <branch> <target_dir> [token]")
            sys.exit(1)
    download_and_extract(repo, branch, target_dir, token)