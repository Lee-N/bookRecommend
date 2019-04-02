function logout() {
    let date=new Date();
    date.setTime(date.getTime()-1);
    document.cookie="userId=0;expires="+date.toUTCString()+"; path=/study";
    window.location.href="login.html"
}