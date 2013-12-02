/* exp4todo.js */
function statuschange(id_num){
	console.log("start");
	var req = new XMLHttpRequest();
	req.open("POST", "statuschange");
	var statusnum = "status" + id_num;
	req.onreadystatechange = function () {
        // readyState == 4: 修了
        if (req.readyState != 4) {
            return;
        }
        // status == 200: 成功
        if (req.status != 200) {
            // 成功しなかった．失敗であることを表示して抜ける．
            alert("失敗．");
            return;
        }
        
        // body にはサーバから返却された文字列が格納される．
        var body = req.responseText;
        // 戻ってきた JSON 文字列を JavaScript オブジェクトに変換
        var data = eval("(" + body + ")");
        // 入力に利用したインプットボックスの値を，サーバから戻ってきた値で書き換える
        document.getElementById(statusnum).textContent = data.result;
    }
    // Content-Type の指定
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");      
    req.send("p=" + enc(id_num));
}
function enc(s) {
    return encodeURIComponent(s).replace(/%20/g, '+');
}