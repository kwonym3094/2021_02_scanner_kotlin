package com.example.scannerkotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.scannerkotlin.DB.table_history.HistoryDatabase
import com.example.scannerkotlin.DB.table_history.HistoryEntity
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var db : HistoryDatabase

    // HistoryActivity 로 부터 받아오는 callback
    // StartActivityForResult 대신 사용하는 Google 권장 API
    private val historyCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                // 화면 비우기
                clearTextViews()
                // 전 화면에서 받아온 정보
                val resultData = result.data
                val strHist = resultData?.getStringExtra("QRCODE")
                // 화면에서 결과 다시 보여주기
                setTextByRules(strHist?:"")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = HistoryDatabase.getInstance(this)!!

        btnHistory.setOnClickListener {
            historyCallback.launch(Intent(this, HistoryActivity::class.java))
        }

        btnScan.setOnClickListener {
            val scanner = IntentIntegrator(this)
            scanner.setPrompt("Scan QR code")
            scanner.captureActivity = CustomScanner::class.java
            scanner.initiateScan()

        }
    }

    // StartActivityForResult 로 부터 결과를 받아오는 추상메소드
    // 구글에서 권장하는 API 방식은 아님(=deprecated)
    //  - 사용한 이유 : QRCode Scan 을 권장하는 zxing 라이브러리가 해당 방식으로 작동하기 때문
    //  - 추후 수정 가능할 듯
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            result?.let {
                val strRaw = result.contents

                // 분할 규칙 적용하기
                setTextByRules(strRaw)

                // 데이터 저장
                insertScan(strRaw)
            }

        }
    }

    // 스캔한 바코드에 비지니스룰 적용하는 메소드
    private fun setTextByRules(strRaw: String) {

        try {
            // 바코드 예시 : [)>06VD003P55210F2BA0SB120EHB1F0613T20122441SAA0000008

            /*
             * 해당 앱 안에서 볼 수 있는 ASCII -> 문자열로 변환해야함
             * 4 -> EOT(End Of Transmission)
             * 29 -> GS(Group Separator)
             * 30 -> RS(Record Separator)
             * */

            // ASCII 코드는 안드로이드 화면에서 볼 수 없음 -> 임의 변경 처리 해줘야함
            // 바꿔야할 문자의 ASCII 코드 및 문자형
            val asciiEot = 4
            val asciiGs = 29
            val asciiRs = 30
            val chEot = asciiEot.toChar().toString().toRegex()
            val chGs = asciiGs.toChar().toString().toRegex()
            val chRs = asciiRs.toChar().toString().toRegex()

            // 바꾸게 될 문자
            val strEot = resources.getString(R.string.endSep)
            val strGs = resources.getString(R.string.grpSep)
            val strRs = resources.getString(R.string.recSep)
            val strOk = resources.getString(R.string.ok)
            val strNg = resources.getString(R.string.ng)

            // 화면에서 보일 수 있게 ASCII 문자 변경
            var strRaw = strRaw.replace(chEot, strEot).replace(chGs, strGs).replace(chRs, strRs)

            rawBarCd.text = Html.fromHtml(strRaw)

            // GS를 기준으로 쪼개기 => 비즈니스 로직에 맞춰서 칸에 넣어주기 위해서
            val arrRaw = strRaw.split(strGs.toRegex()).toTypedArray()

            // 다음 규칙은 해당 비즈니스에만 해당되는 사항임 (즉, 목적에 따라 유동적으로 변경될 수 있음)
            //  - header 규칙: 1) "[)>RS~~" 으로 시작, 2) 앞의 단어 포함 뒤에 2자리 더 나옴
            //  - footer 규칙: 1) "RSEOT"임
            val strHeaderRule = "[)>${strRs}"
            val strFooterRule = "${strRs}${strEot}"

            // 첫번째 분절과 마지막 분절의 규칙이 맞는지 확인 (전체 형식 확인)
            if (isHeaderRight(arrRaw[0], strHeaderRule) && isFooterRight(
                    arrRaw[arrRaw.size - 1],
                    strFooterRule
                )) {
                // 전체 분절이 7개가 맞는지 확인
                if (arrRaw.size != 7) {
                    // 7개가 아닐때 -> NG 반환
                    setAllNG(strNg)
                } else {
                    // 분절 각각의 규칙 확인 (세부 내용 확인)
                    for (i in arrRaw.indices) {

                        var results = arrayOfNulls<String>(2)

                        when (i) {
                            1 -> results = getResultByFirstChar(arrRaw[i], "V")
                            2 -> results = getResultByFirstChar(arrRaw[i], "P")
                            3 -> results = getResultByFirstChar(arrRaw[i], "S")
                            4 -> results = getResultByFirstChar(arrRaw[i], "E")
                            5 -> results = getResultByFirstCharAndLen(arrRaw[i], "T", 19)
                            0, 6 -> {
                                results[0] = strOk
                                results[1] = arrRaw[i]
                            }
                        }

                        when (i) {
                            in 0..4 -> {
                                // 해당 id를 갖는 TextView 를 찾아서 Text 삽입
                                setTextToTextViewByIdNo(results[0]!!, "result", i)
                                setTextToTextViewByIdNo(results[1]!!, "data", i)
                            }
                            5 -> {
                                val arrSepPoint = intArrayOf(0, 6, 10, 11, results[1]!!.length)
                                for (j in 0..3) {

                                    // 5번째 분절의 2차 규칙 적용
                                    //  - 현재는 규칙없이 자르기만 진행
                                    val strSecChunk =
                                        results[1]!!.substring(arrSepPoint[j], arrSepPoint[j + 1])
                                    setTextToTextViewByIdNo(results[0]!!, "result", i + j)
                                    setTextToTextViewByIdNo(strSecChunk, "data", i + j)
                                }
                            }
                            6 -> {
                                setTextToTextViewByIdNo(results[0]!!, "result", i + 3)
                                setTextToTextViewByIdNo(results[1]!!, "data", i + 3)
                            }
                        }
                    }
                }
            } else {
                // 전체 형식이 맞지 않을 때 => 모든값을 NG 처리
                setAllNG(strNg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    // 화면에 모두 NG 결과 보여주는 메소드
    private fun setAllNG(strNg: String) {
        for (i in 0..9) {
            setTextToTextViewByIdNo(strNg, "result", i)
        }
    }

    // Header 규칙에 상응하는지 확인 후 결과 리턴
    private fun isHeaderRight(strScan: String, strHeaderRule: String): Boolean =
        (strScan.startsWith(strHeaderRule)) && (strScan.length == strHeaderRule.length + 2)

    // Footer 규칙에 상응하는지 확인 후 결과 리턴
    private fun isFooterRight(strScan: String, strFooterRule: String): Boolean =
        (strScan == strFooterRule)


    // 시작 글자에 맞는 규칙 적용한 후 결과 리턴
    private fun getResultByFirstChar(arrChunk: String, firstChar: String): Array<String?> {
        val results = arrayOfNulls<String>(2)
        if (arrChunk.substring(0, 1) == firstChar) {
            results[0] = resources.getString(R.string.ok)
            try {
                results[1] = arrChunk.substring(1)
            } catch (e: IndexOutOfBoundsException) {
                results[1] = ""
            }
        } else {
            results[0] = resources.getString(R.string.ng)
            results[1] = arrChunk
        }
        return results
    }

    // 시작 글자에 맞는 규칙, 글자 길이 검사 적용한 후 결과 리턴
    private fun getResultByFirstCharAndLen(
        arrChunk: String,
        firstChar: String,
        len: Int
    ): Array<String?> {
        val results = arrayOfNulls<String>(2)
        if (arrChunk.substring(0, 1) == firstChar && arrChunk.length >= len) {
            results[0] = resources.getString(R.string.ok)
            try {
                results[1] = arrChunk.substring(1)
            } catch (e: IndexOutOfBoundsException) {
                results[1] = ""
            }
        } else {
            results[0] = resources.getString(R.string.ng)
            results[1] = arrChunk
        }
        return results
    }

    // (숫자가 증가하며 반복되는) 아이디 찾아서 Text 넣어주는 메소드
    private fun setTextToTextViewByIdNo(text: String, id_name: String, iter_num: Int) {

        // 해당 id를 가지는 리소스 찾기
        val resID = resources.getIdentifier(id_name + iter_num, "id", packageName)

        // 위에서 찾은 리소스에 값 변경해주기
        (findViewById<View>(resID) as TextView).text = Html.fromHtml(text)
    }

    // TextView Text 초기화
    private fun clearTextViews() {
        rawBarCd.text = ""
        for (i in 0..9) {
            setTextToTextViewByIdNo("", "result", i)
            setTextToTextViewByIdNo("", "data", i)
        }
    }

    private fun insertScan(strRaw: String){
        GlobalScope.launch {

            val date = Date()
            val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val ymd = format1.format(date)


            val entity = HistoryEntity(null, strRaw, ymd)
            db.historyDAO().insert(entity)

        }
    }

}