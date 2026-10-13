package io.legado.app.help.config

import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.PaintCompat

/**
 * 界面字体缺字检测（A 层字符集）。
 *
 * 字符集 = 四个 `strings.xml`（values / values-zh / values-zh-rTW / values-zh-rHK）去重后的
 * CJK 字符（含全角标点）+ 常用 ASCII（数字 / 字母 / 常见标点），合计 1210 个字符，其中汉字 1100 个。
 * 由脚本从源码提取生成，未手写。
 *
 * 判定规则：**缺失 > 0 即拒绝应用该字体**（必显文案不允许缺字）。
 *
 * 适用范围：仅 Android 9 及以下（API 23–28）。API 29+ 走
 * `Typeface.CustomFallbackBuilder(...).setSystemFallback("sans-serif")`，
 * 缺字自动回退系统字体，无需检测。
 *
 * 已知局限：`Typeface.hasGlyph()` 只查字体里有无该字形编号，部分字体「有编号但字形空白」，
 * 会漏判。本字符集也覆盖不到运行时数据（书名 / 作者 / 书源名等）。
 */
internal object UiFontGlyphCheck {

    /** 必检字符集 */
    val requiredChars: String = buildString {
        append(" !\"#\$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[")
        append("\\]^_`abcdefghijklmnopqrstuvwxyz{|}~、。《》「」【】一三上下不与且丢两並个中串临为主久")
        append("义之乐也书买亂了予事二于云互五些交产享亮人什仅今介仍从他付代令以们件任份仿众优会传伺似但佇佈位住体佔余作你佬佳併使來例供")
        append("依侧便係保信修個倍們倒候值停側偵備储傳僅像優儲允元充先光免入內全兩公六共关其具典养内冇册再写冲净准凈减凑出击函分切划列刘")
        append("则创初删判別刪别到制刷則前剩剪割創劉力功加务动助势動務勢勾包化匯匹区區半协卓協单占卡即卷历压原去参參及双反发取受变叠口句")
        append("只叫可台史右号各合同名后向吗否吧含听启告员呼命和咗咪咭品响員唔唤問啓啟喚單喺嗎嘅器四回因围图圆圍圖在地址均坏块坚型埋城域")
        append("埠執基堅堆填增墨壁壓壞声壹处备复外多夜大天太失头夹夾奈套好如始媒子字存它安完定宝实容密實寫寶对导封射将將尋對導小少尚就尾")
        append("局居屏展崩工左已布帖带帮帳帶常幕幫平年并广序库应底度庫廣延建开异式引強弹强彈当录彙形影彻往径待後徑從復微徹志快忽态性总恢")
        append("息您悬情想意感態慎慧憑憶應懒懸成我或截戳戶户所手才打执扩扫批找承把抗折拆拉拒拖拟择括拷拽持按捐损换据捲捷掃授排接控推描提")
        append("插換援損搜携摘摸摺撤播擇擊操擎據擬擴支收改攻放效敏敗数整數文料断新斷方於旁无日旧旨早时明映昨是显時晖普景智暂暗暫曆更書替")
        append("最會月有服朗期未本机杀权材束条来板析果架某染柔查标栏校样根格框案桌档條检榜樂標模樣横機橫檔檢欄權次欢款歡止正此步歷殊段殺")
        append("每比毫气氣水永求汇沉沒没法泡注活流浅测浏浮海浸消淨深混添清渊減測渲溃源準溢滑滚满滤滾滿漫潰濾瀏瀑灰点為無焦然片版牌物特状")
        append("狀獲率现玻現球理璃生產用由电画界留略畫異當疊登發白的监盖盘目直相眉看真眾着矢知短码砂确確碼磁磨示禁离私种秒称移程稍種稱空")
        append("突窗立竖站章端笔符第等筛签简算管節範篇篩簡簽簿籍籤类粗粘精系紅純紙級素索紧累細組結絕絡給統經綜綠維網緊緒線編緩縮總繁繪繫")
        append("繼續红级纯纸线组细经结绘给络绝统继续维综绿缓编缘缩缺网置美羣群義翻者耗耳联聚聯聲肥背能脚腳臨自至致臺與舊航般色节花若英范")
        append("荐莫获菜著蓋蔽藏處虚虛號螢行衝补表被装裏補裝裡製複要覆見規視覽见规视览角解触觸言訂計訊記訪設許註評詞詢試話該詳誌認語誤說")
        append("説調請謹證識警議讀變讓讚计订认让议记许设访证评识词试话询该详语误说请读调谨豎象貝責買費貼資賞賬購贈贝责败账质购贴费资赏赞")
        append("赠起超越足跃距跟路跳踩躍軟較載輔輪輯輸轉转轮软轴载较辅辑输边达迁过迎运近返还这进远连迟述追退送适选透逗這通速連進運過道達")
        append("遠適遭遷選還邊邮部郵都配醒释釋里重量鈕銷鋸錄錯鍵鎖鏈鏡鐘钟钮链销锁错键锯镜長长閂閉開間閱閲閾關闭问间阅阈队阴阶附陆限除陰")
        append("陸隊階随隐隔隨隱集雙離雲電需静非靠面音響頁頂項順預頓領頭頻題顏類顯页顶项顺顿预领频题颜風风養餘首驗驟验骤體高麼黑默點鼠齊")
        append("齐齒齿！（），：；？")
        }

    /**
     * 返回该字体**缺失**的字符列表；为空表示全部具备。
     *
     * 调用方须在后台线程执行：1210 次 `hasGlyph` 约几十毫秒。结果建议按字体文件
     * SHA-256 缓存，避免重复解码。
     */
    fun missingChars(typeface: Typeface): List<Char> {
        // 用 Paint 而不是 Typeface：hasGlyph 只在 Paint / PaintCompat 上提供，
        // Typeface 本身没有这个方法。这里探测的 Typeface 是单字体（无回退链），
        // 所以结果就是该字体自身的覆盖情况，不会被系统字体补漏掩盖。
        val paint = Paint().apply { this.typeface = typeface }
        val missing = ArrayList<Char>()
        for (c in requiredChars) {
            if (!PaintCompat.hasGlyph(paint, c.toString())) {
                missing.add(c)
            }
        }
        return missing
    }

    /** 是否可安全应用：无任何缺失字符 */
    fun isUsable(typeface: Typeface): Boolean = missingChars(typeface).isEmpty()
}
