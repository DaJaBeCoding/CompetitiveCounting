var API_ENDPOINT = 'https://discord.com/api/v8';
var CLIENT_ID = '898161954378641418';
var CLIENT_SECRET = '9vaSLF_uQu0rfnR_3jY2bCjhsk4HLXnk';
var REDIRECT_URI = 'https://dajabecoding.github.io/CompetitiveCounting';


// JavaScript source code
var url_string = window.location.href;
var url = new URL(url_string);
var code = url.searchParams.get("code");

if (code !== null) {
    console.log(exchangeCode(code));
}

function exchangeCode(code) {
    var http = new XMLHttpRequest();
    http.open('POST', API_ENDPOINT + '/oauth2/token', true);
    var data = {
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_SECRET,
        'grant_type': 'authorization_code',
        'code': code,
        'redirect_uri': REDIRECT_URI
    }
    http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    http.onreadystatechange = function () {
        if (http.readyState == 4 && http.status == 200) {
            alert(http.responseText);
        }
    }
    http.send(new URLSearchParams(data))
}
