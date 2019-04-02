$(function () {
    check();
})

//验证登录态
function check() {
    $.ajax({
        url:url,
        dataType:"json",
        xhrFields: {
            withCredentials: true
        },
        data:{
            method:"check",
        },
        success:function (data) {
            if(data.error_code==0){
                window.location.href="index.html"
            }
        }
    })
}


//登录
function login() {
    if($("#form").valid()){
        $.ajax({
            url:url,
            dataType: "json",
            xhrFields: {
                withCredentials: true
            },
            data:{
                method:"login",
                username:$("#username").val(),
                password:$("#password").val()
            },
            success:function (data) {
                if(data.error_code==0){
                    window.location.href="index.html"
                }
                else {
                    swal(
                        "用户名或密码错误",
                        "",
                        "error"
                    )
                }
            }
        })
    }

}