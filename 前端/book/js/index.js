
let books=new Array();
let bookId=0;
let rates=new Map();
$(function () {
    recommend();
    rate();
    getRate();
})



//加载推荐
function recommend() {
    $.ajax({
        url:url,
        dataType:"json",
        xhrFields: {
            withCredentials: true
        },
        data:{
            method:"recommend"
        },
        success:function (data) {
            if(data.error_code==1){
                swal({
                    title:"登录状态失效",
                    type:"error"
                },function (isConfirm) {
                    if(isConfirm)
                        window.location.href="login.html"
                })
            }
            let size=data.data.length>=12?12:data.data.length;//如果不够12本书就调整size为 书的数量
            let html=`<div class="row">`
            for(let i=0;i<size;i++){
                books.push(data.data[i])
                html+=`<div class="col-md-2 col-sm-2 col-xs-2 "><img index="${i}" src="${data.data[i].img}"></div>`
                //如果是第六本书闭合row并且最后一本书不增加新的row
                if((i+1)%6==0&&i!=size-1){
                    html+=`</div><div class="row">`
                }
            }
            html+=`</div>`;
            $(".recommend").append(html)

            //为图片绑定点击事件
            $(".recommend img").on("click",function () {
                let index=$(this).attr("index");
                showDetail(index);
            })
            console.log(books)
        },
    })
}

//显示详细信息
function showDetail(index) {

    let book=books[index];
    bookId=book.bookId;
    addHot();
    // $("#ISBN").text(book.ISBN);
    $("#publisher").text(book.publisher)
    $("#name").text(book.bookName)
    $("#author").text(book.author);
    $("#img").prop("src",book.img);
    $("#description").val(book.description);
    $("#rate").rating('update',rates.get(bookId))
    $("#detail").modal("show")
}

function search() {
    let content=$("#content").val();
    window.location.href="result.html?content="+decodeURI(content);
}

//评分
function rate() {
    $("#rate").rating({
        min:0,
        max:5,
        showClear:false,
        step:1,
        size:"xs",
        defaultCaption: '{rating} 星',
        starCaptions: {
            0.5: '半星',
            1: '一星',
            1.5: '一星半',
            2: '二星',
            2.5: '二星半',
            3: '三星',
            3.5: '三星半',
            4: '四星',
            4.5: '四星半',
            5: '五星'
        },
        clearButtonTitle: '清除',
        clearCaption: '未评级'
    })

    //绑定事件
    $('#rate').on('rating.change', function(event, value, caption) {
        $.ajax({
            url:url,
            dataType:"json",
            xhrFields: {
                withCredentials: true
            },
            data:{
                method:"rate",
                bookId:bookId,
                rate:value
            },
            success:function () {
                getRate();
            },
            error:function () {
                swal({
                    title:"评分失败",
                    type:"error"
                })
            }
        })

    });
}


//获取用户对图书的所有评分
function getRate() {
    $.ajax({
        url:url,
        dataType:"json",
        xhrFields: {
            withCredentials: true
        },
        data:{
            method:"getRate"
        },
        success:function (data) {
            for(let i=0;i<data.data.length;i++){
                rates.set(data.data[i].bookId,data.data[i].rate)
            }
        }

    })
}

// 增加图书热度
function addHot() {
    $.ajax({
        url:url,
        dataType:"json",
        xhrFields: {
            withCredentials: true
        },
        data:{
            method:"addHot",
            bookId:bookId
        }
    })
}
