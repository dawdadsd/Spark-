import threading
from time import time
from flask import Blueprint, jsonify, request
from app.file.func import GetCaseFileList
from core.controllers.case.func.base import GetCaseList, GetCaseName
from core.controllers.case.vaild import check_case_valid
from core.controllers.task import checkTaskCreate, getTaskList, newTask
from core.controllers.users.func import getCurrentUserUID
from core.models.dataAnalyse.models import ModelManager
from core.models.dataAnalyse import DataAnalyse
from core.controllers.database import db
from core.models.dataImport import ImportData


data_processing_bp = Blueprint("data_processing", __name__)


@data_processing_bp.route("/api/data/models")
def get_models():
    ModelList = ModelManager().get_model_files()
    return jsonify({"success": True, "message": "获取成功", "data": ModelList})


@data_processing_bp.route("/api/data/caseData", methods=["POST"])
def caseData():
    if not request.is_json:
        return jsonify({"success": False, "message": "非法请求", "data": []})
    caseId = request.get_json().get("caseID")
    modelName = request.get_json().get("menuTitle")
    result = {
        "success": True,
        "pageSize": "10",
        "current": "1",
    }
    flow = DataAnalyse(caseId, modelName)
    caseData = flow.analyze()
    result.update(caseData)
    return jsonify(result)


@data_processing_bp.route("/api/data/<caseID>/total")
def dataCount(caseID):
    if not caseID:
        return jsonify({"success": False, "message": "非法请求", "data": []})
    total = 0
    translations = 0
    userNums = 0
    if caseID == "all":
        [suc, data] = db.querySql(
            """SELECT SUM(table_rows) AS total_rows,(SELECT SUM(table_rows) FROM information_schema.tables WHERE table_name like '%资金交易%') AS total_trans,(SELECT SUM(table_rows) FROM information_schema.tables WHERE table_name like '%用户信息综合数据%') as userNums FROM information_schema.tables
                WHERE table_schema = 'flowdata' AND table_name NOT like '%table%';
                """
        )
        if suc:
            total = data[0].get("total_rows", 0)
            translations = data[0].get("total_trans", 0)
            userNums = data[0].get("userNums", 0)
    else:
        if not check_case_valid(caseID):
            return jsonify({"success": False, "message": "非法请求", "data": {}})
        [suc, data] = db.querySql(
            """SELECT
                	SUM( table_rows ) AS total_rows,(
                	SELECT
                		SUM( table_rows ) 
                	FROM
                		information_schema.TABLES 
                	WHERE
                		table_name LIKE '%{caseID}_资金交易%' 
                		) AS total_trans,(
                	SELECT
                		SUM( table_rows ) 
                	FROM
                		information_schema.TABLES 
                	WHERE
                		table_name LIKE '%{caseID}_用户信息综合数据%' 
                	) AS userNums 
                FROM
                	information_schema.TABLES 
                WHERE
                	table_schema = 'flowdata' 
                	AND table_name NOT LIKE '%table%';
                """.format(
                caseID=caseID
            )
        )
        if suc:
            total = data[0].get("total_rows", 0)
            translations = data[0].get("total_trans", 0)
            userNums = data[0].get("userNums", 0)
    return jsonify(
        {
            "success": True,
            "message": "获取成功",
            "data": {
                "total": total,
                "translations": translations,
                "userNums": userNums,
            },
        }
    )


@data_processing_bp.route("/api/data/<caseID>/fileList")
def fileList(caseID):
    caseList = []
    fileList = []
    resultList = []
    if not check_case_valid(caseID) and caseID != "all":
        return jsonify({"success": False, "message": "非法请求", "data": {}})
    if caseID == "all":
        caseList = [case["case_uid"] for case in GetCaseList()]
    else:
        caseList = [caseID]
    for case in caseList:
        files = GetCaseFileList(case)
        for file in files:
            resultList.append(
                {
                    "caseName": GetCaseName(case),
                    "case_uid": case,
                    "file_address": file.get("fileName"),
                    "file_data_num": 123456,
                    "template": "测试",
                }
            )
    return jsonify({"success": True, "message": "获取成功", "data": resultList})


@data_processing_bp.route("/api/data/<caseID>/import", methods=["POST"])
def dataImport(caseID):
    # ipd = ImportData(caseID)
    # threading.Thread(target=ipd.run).start()
    if checkTaskCreate(caseID, getCurrentUserUID()):
        return jsonify({"success": False, "message": "任务已经存在,请勿重复提交", "data": {}})
    newTask(caseID, getCurrentUserUID(), "导入数据", "import")

    return jsonify({"success": True, "message": "数据导入后台开始执行!", "data": {}})


@data_processing_bp.route("/api/data/<caseID>/uploadTask")
def uploadTaskList(caseID):
    if not check_case_valid(caseID) and caseID != "all":
        return jsonify({"success": False, "message": "非法请求", "data": {}})
    result = []
    taskList = getTaskList(caseID)
    for task in taskList:
        result.append(
            {
                "caseName": GetCaseName(task["case_uid"]),
                "case_uid": task["case_uid"],
                "task_name": task["taskName"],
                "startTime": task["createTime"],
                "status": task["status"],
            }
        )
    return jsonify({"success": True, "message": "获取成功", "data": result})
