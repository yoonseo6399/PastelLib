package io.github.yoonseo.pastellib.utils.entity.model

import io.github.yoonseo.pastellib.PastelLib
import java.io.File
import java.io.IOException

class ModelFileManager {
    companion object {
        val ROOT = PastelLib.instance.dataFolder
    }
    fun saveModel(modelName : String , modelData : List<DisplayData>) {
        try {
            // 폴더 경로를 File 객체로 생성
            val folder = ROOT

            // 폴더가 존재하지 않으면 생성
            if (!folder.exists()) {
                folder.mkdirs()  // 폴더 생성
            }

            // 파일 경로를 File 객체로 생성
            val file = File(folder, "$modelName.model" )

            // 파일에 내용을 기록
            file.writeText(PastelLib.json.encodeToString(modelData))

            println("파일이 성공적으로 생성되었습니다: ${file.absolutePath}")
        } catch (e: IOException) {
            println("파일 생성 중 오류 발생: ${e.message}")
        }
    }
    fun loadModelData(modelName: String) : List<DisplayData>{
        try {
            // 파일 경로를 File 객체로 생성
            val file = File(ROOT, "$modelName.model")

            // 파일이 ���재하지 않으면 null 반환
            if (!file.exists()) {
                return emptyList()
            }

            // 파일 내용을 ��어 JSON object로 변환
            val jsonData = file.readText()

            // JSON object를 List<DisplayData>로 변환
            return PastelLib.json.decodeFromString(jsonData)
        } catch (e: IOException) {
            println("파일 로드 중 오류 발생: ${e.message}")
            return emptyList()
        }
    }
}