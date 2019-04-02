let content = decodeURI(getQueryVariable("content")).toString();
let books = new Array();
let bookId=0;
let pageSize = 8;
let totalCounts = 0;
let rates=new Map();
$(function () {
    getResult();
    rate();
    getRate();
})


//获取url参数
function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0] == variable) {
            return pair[1];
        }
    }
    return (false);
}


//得到搜索结果
function getResult() {
    $.ajax({
        url: url,
        dataType: "json",
        xhrFields: {
            withCredentials: true
        },
        data: {
            method: "search",
            content: content
        },
        success: function (data) {
            if(data.error_code==1){
                swal({
                    title:"登录状态失效",
                    type:"error"
                },function (isConfirm) {
                    if(isConfirm)
                        window.location.href="login.html"
                })
            }
            books = data.data;
            totalCounts = books.length;
            $("#pagination").jqPaginator({
                totalCounts: totalCounts,
                pageSize: pageSize,
                visiblePages: 5,
                onPageChange: function (num, type) {
                    render(num)
                }
            })
        }
    })
}

function render(index) {
    $(".books").empty()
    let lastPage = (index - 1) * pageSize;//起始下标
    let currentPage = index * pageSize;//本页下标
    if (currentPage > totalCounts) currentPage = totalCounts;
    let html = "";
    for (let i = lastPage; i < currentPage; i+=2) {
        html += `<div class="row">
                  <div index="${i}" class="col-sm-6 book">
                        <div class="col-sm-4 "><img src="${books[i].img}"></div>
                        <div class="row col-sm-8">
                            <label>图书名称</label><h5>${books[i].bookName}</h5>
                            <hr width="100%">
                            <label>作者</label><h5>${books[i].author}</h5>
                        </div>
                     </div>`
        if (i+1 == currentPage&&(i%2)==0) {
            html += `</div>`
        } else {
            html += `<div index="${i+1}" class="col-sm-6 book">
                        <div class="col-sm-4 "><img src="${books[i+1].img}"></div>
                        <div class="row col-sm-8">
                            <label>图书名称</label><h5>${books[i+1].bookName}</h5>
                            <hr width="100%">
                            <label>作者</label><h5>${books[i+1].author}</h5>
                        </div>
                     </div>
                 </div>`
        }
    }
    $(".books").append(html);
    //为每本书绑定点击事件
    $(".books .book").on("click",function () {
        let index=$(this).attr("index");
        console.log(index)
        showDetail(index);
    })
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
                alert("评分失败")
            }
        })

    });
}


//获取用户图书的所有评分
function getRate() {
    $.ajax({
        url:url,
        dataType:"json",
        xhrFields: {
            withCredentials: true
        },
        data:{
            method:"getRate",
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

//显示详细信息
function showDetail(index) {

    let book=books[index];
    console.log(index);
    bookId=book.bookId;
    addHot();
    $("#ISBN").text(book.ISBN);
    $("#publisher").text(book.publisher)
    $("#name").text(book.bookName)
    $("#author").text(book.author);
    $("#img").prop("src",book.img);
    $("#description").val(book.description);
    $("#rate").rating('update',rates.get(bookId))
    $("#detail").modal("show")
}

//验证登录态
function check() {
    $.ajax({
        url:url,
        dataType:"json",
        async:false,
        xhrFields: {
            withCredentials: true
        },
        data:{
            method:"check",
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
        }
    })
}

