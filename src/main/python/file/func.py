from config import PROJECT_FILE
import os
from operator import itemgetter
from datetime import datetime

UPLOAD_FOLDER = f"{PROJECT_FILE}/uploads/data"


def GetCaseFileList(caseID):
    case_folder_path = os.path.join(UPLOAD_FOLDER, caseID)

    def get_file_info(file_path):
        """
        获取文件信息
        """
        file_stat = os.stat(file_path)
        if file_stat.st_size > 1024 * 512:
            file_size_mb = file_stat.st_size / (1024 * 1024)  # 转换为MB
            file_size_name = f"{file_size_mb:.2f}MB"
        else:
            file_size_kb = file_stat.st_size / 1024  # 转换为KB
            file_size_name = f"{file_size_kb:.2f}KB"

        file_info = {
            "fileName": os.path.basename(file_path),
            "fileDate": datetime.fromtimestamp(file_stat.st_mtime).strftime(
                "%Y-%m-%d %H:%M:%S"
            ),  # 格式化文件的最后修改时间
            "fileType": os.path.splitext(file_path)[1],  # 获取文件扩展名
            "fileSize": f"{file_size_name}",  # 格式化文件大小为字符串
        }
        return file_info

    def list_files(root_dir="."):
        """
        遍历所有子目录下的所有文件，并返回文件信息列表
        """
        file_info_list = []
        for foldername, subfolders, filenames in os.walk(root_dir):
            for filename in filenames:
                file_path = os.path.join(foldername, filename)
                file_info = get_file_info(file_path)
                file_info_list.append(file_info)
        return file_info_list

    # 遍历当前目录下所有子目录下的所有文件
    files_info = list_files(case_folder_path)
    sorted_files = sorted(files_info, key=itemgetter("fileSize"), reverse=True)

    # 只显示前10列
    top_10_files = sorted_files[:10]
    return top_10_files
