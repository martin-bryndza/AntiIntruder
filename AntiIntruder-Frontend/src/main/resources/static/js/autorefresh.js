var thetime = 10; // this is the time you want in seconds for the page to reload in
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
        createCookie('isChecked', 'true', 9999);
        var startRefresh = setTimeout("window.location.reload()", thetime * 1000);
    }
    else
    {
        eraseCookie('isChecked');
        clearTimeout(startRefresh);
    }
}
window.onload = function()
{
    document.autorefresh.dorefresh.checked = (readCookie('isChecked') === 'true') ? 1 : 0;
    autoRefresh();
};