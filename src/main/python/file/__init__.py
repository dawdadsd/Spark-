from time import sleep
import uuid
from flask import Blueprint, request, jsonify
import os
import pandas as pd
import zipfile
from config import UPLOAD_FOLDER
from core.controllers.zipper import ZipNamecode
from core.controllers.zipper.task import UnzipTaskQueue
from core.tools.logs import logger

file_upload_bp = Blueprint("file_upload", __name__)

ALLOWED_EXTENSIONS = {"xlsx", "xls", "csv", "zip"}


def allowed_file(filename):
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS


@file_upload_bp.route("/api/upload/<case_id>/data", methods=["POST"])
def upload_file(case_id):
    if "file" not in request.files:
        return jsonify({"error": "无文件"})

    file = request.files["file"]

    if file.filename == "":
        return jsonify({"success": False, "error": "No selected file"})

    # 检查文件类型是否允许
    if not allowed_file(file.filename):
        return jsonify(
            {"success": False, "message": "无效的文件类型。只允许使用xlsx、xls、csv或zip文件。"}
        )

    # 检查案件ID对应的文件夹是否存在，如果不存在就创建
    case_folder = os.path.join(UPLOAD_FOLDER, case_id)
    os.makedirs(case_folder, exist_ok=True)
    # 保存文件到案件ID对应的文件夹
    file_path = os.path.join(case_folder, file.filename)
    if file.filename.endswith(".zip"):
        new_zip_name = str(uuid.uuid4()) + ".zip"
        os.makedirs(os.path.join(case_folder, "zip"), exist_ok=True)
        file_path = os.path.join(case_folder, "zip", new_zip_name)
        file.save(file_path)
        sleep(0.5)  # 等待0.5秒，防止文件没有写入完成
        # # 读取zip文件,并且判断是否里面的文件列表只有xlsx、xls、csv
        try:
            zip_file = zipfile.ZipFile(file_path, "r")
            zip_file_list = zip_file.namelist()
            zip_file.close()
            for zip_file_name in zip_file_list:
                if not zip_file_name.endswith("/"):
                    if not allowed_file(zip_file_name):
                        os.remove(file_path)
                        return jsonify(
                            {
                                "success": False,
                                "message": f"ZIP内有非法文件 文件名:{ZipNamecode(zip_file_name)},只允许使用xlsx、xls、csv。",
                            }
                        )
            # UnzipTaskQueue().submit(case_id, new_zip_name)
        except zipfile.BadZipFile:
            os.remove(file_path)
            return jsonify({"success": False, "message": "无效的ZIP文件。"})
    elif file.filename.endswith(".csv") or file.filename.endswith(".xlsx") or file.filename.endswith(".xls"):
        file.save(file_path)
    return jsonify({"success": True, "message": "文件上传成功", "case_id": case_id})


@file_upload_bp.route("/api/upload/<caseID>/readUploadData", methods=["POST"])
def preViewData(caseID):
    if not request.is_json:
        return jsonify({"success": False, "message": "非法请求", "data": []})
    filename = request.get_json().get("filename")
    view_data_path = os.path.join(UPLOAD_FOLDER, caseID, filename)
    if os.path.exists(view_data_path):
        if filename.endswith(".csv"):
            df = pd.read_csv(view_data_path, encoding="GB18030")
        data = df.head(10).to_dict(orient="records")
        return jsonify(
            {
                "success": True,
                "message": "获取成功",
                "data": {"keys": df.keys().to_list(), "list": data},
            }
        )
    return jsonify({"success": False, "message": "文件不存在", "data": []})
