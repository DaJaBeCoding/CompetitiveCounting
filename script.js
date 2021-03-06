var API_ENDPOINT = 'https://discord.com/api/v8';
var CLIENT_ID = '898161954378641418';
var CLIENT_SECRET = '9vaSLF_uQu0rfnR_3jY2bCjhsk4HLXnk';
var REDIRECT_URI = 'https://dajabecoding.github.io/CompetitiveCounting';


// JavaScript source code
var url_string = window.location.href;
var url = new URL(url_string);
var code = url.searchParams.get("code");
document.getElementById("avatar").hidden = true;
if (code !== null) {
    console.log(code);
    exchangeCode(code);
}

function handleAuthResponse(response) {
    console.log(response);
    var dict = JSON.parse(response);
    var type = dict["token_type"];
    var token = dict["access_token"];
    console.log(type);
    console.log(token);
    getUsername(type, token);
}

function handleUsernameResponse(response) {
    var resJson = JSON.parse(response);
    document.getElementById("nameLbl").innerHTML = resJson["user"]["username"] + "#" + resJson["user"]["discriminator"];
    var user_id = resJson["user"]["id"];
    var user_avatar = resJson["user"]["avatar"];
    if (user_avatar !== null && user_avatar !== "none") {
        document.getElementById("avatar").src = "https://cdn.discordapp.com/avatars/" + user_id + "/" + user_avatar + ".png";
        document.getElementById("avatar").hidden = false;
    }
}

function getUsername(accessType, accessCode) {
    var http = new XMLHttpRequest();
    http.open('GET', "https://discord.com/api/oauth2/@me");

    http.setRequestHeader('authorization', accessType + " " + accessCode);
    http.onreadystatechange = function () {
        if (http.readyState == 4 && http.status == 200) {
            handleUsernameResponse(http.responseText);
        }
    }
    http.send();
}

function exchangeCode(code) {
    var http = new XMLHttpRequest();
    http.open('POST', "https://discord.com/api/oauth2/token");
    http.responseType = 'text';
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
            handleAuthResponse(http.responseText);
        }
    }
    http.send(dictToURI(data));
}
function dictToURI(dict) {
    var str = [];
    for (var p in dict) {
        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(dict[p]));
    }
    return str.join("&");
}
