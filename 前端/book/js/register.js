$(function () {

    $("#form").validate({
        rules:{
            confirm:{
                equalTo:"#password"
            }
        }
    })
    //绑定添加爱好
    $(document).on("click",".hobbies label",function () {
        $("#area").append($(this))
    })

    //绑定移除爱好
    $(document).on("click","#area label",function () {
        $(".hobbies").append($(this))
    })
})

function register() {
    if($("#form").valid()){
        if($("#area").children().length==0)
            swal({
                title:"至少选择一个爱好",
                type:"error"
            })
        else{
            let hobby=""
            $("#area").children().each(function () {
                hobby+=$(this).text().substring(1,$(this).text().length-1)//去除开始和结束的空格
            });
            hobby=hobby.substring(1)
            $.ajax({
                url:url,
                dataType:"json",
                data:{
                    method:"register",
                    name:$("#name").val(),
                    username:$("#username").val(),
                    password:$("#password").val(),
                    hobby:hobby
                },
                success:function (data) {
                    if(data.error_code==0){
                        swal({
                                title:"注册成功",
                                type:"success"
                        },function (isConfirm) {
                            if(isConfirm) {
                                window.location.href = "login.html";
                            }
                        })

                    }
                    else{
                        swal(
                            "用户名已存在",
                            "",
                            "error"
                        )
                    }
                },
                error:function () {
                    swal(
                        "系统错误",
                        "",
                        "error"
                    )
                }
            })
        }

    }
}

