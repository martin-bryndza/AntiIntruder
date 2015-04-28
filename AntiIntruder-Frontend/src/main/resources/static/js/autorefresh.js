var thetime = 10; // this is the time you want in seconds for the page to reload in
var startRefresh;
function createCookie(name, value, days)
{
    if (days)
    {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        var expires = "; expires=" + date.toGMTString();
    }
    else
        var expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
}

function readCookie(name)
{
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ')
            c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0)
            return c.substring(nameEQ.length, c.length);
    }
    return null;
}

function eraseCookie(name)
{
    createCookie(name, "", -1);
}
function autoRefresh()
{
    if (document.autorefresh.dorefresh.checked === true)
    {
        eraseCookie('isChecked');
        startRefresh = setInterval("retrieveColleagues()", thetime * 1000);
    }
    else
    {
        createCookie('isChecked', 'false', 9999);
        clearInterval(startRefresh);
    }
}
window.onload = function()
{
    document.autorefresh.dorefresh.checked = (readCookie('isChecked') === 'false') ? 0 : 1;
    autoRefresh();
};