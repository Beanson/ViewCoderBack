//装载数据的容器
var windowsWidth = $(window).width();
var data = {
    /*空项目需用到的数据*/
    'all_tools': {
        //数据流组件
        "Table": {},
        "List": {},
        //通用组件
        "Common_Image": {},
        "Common_Text": {},
        "Common_Background": {},
        "Common_Button": {},
        //表单组件
        "TextInput": {},
        "Password": {},
        "TextArea": {},
        "CheckBox": {},
        "RadioBox": {},
        "SelectOptions": {},
        "DatePicker": {},
        "File": {},
        "Submit": {},
        //多媒体组件
        "Video": {},
        "Sound": {},
        "Carousel": {},
        //其他组件
        "DownLoad": {},
        "List_Navigation": {},
        "CusCode": {}
    },
    /*空项目和PSD项目会用到的数据*/
    'overall': {
        'width': windowsWidth, //会依据屏幕宽度而重新赋值该width，默认是1300，修改默认值时级联修改BasicDataSer.defaultConfig['width']['computer']
        'height': $(document).height(),
        'is_mobile': false, //标识是否是mobile网页，默认是PC网页
        'scale': false,
        'bg-color': 'rgba(250,0,0,0.04)',
        'max_id': 1,
        'max_rate': 1,
        'public': false, //是否为public
        'industry': '', //所属行业
        'usage_amount': 0,  //使用次数
    }
};


var divs = $('div'); //所有div组件获取数据
var imgs = $('img'); //获取所有image组件
var spans = $('span, a, p, h1, h2, h3, h4, h5, h6, dt, dd, caption, th, td'); //获取所有装载text组件
var buttons = $('button');
var textareas = $('textarea');
var inputs = $('input');
var num = 0, retrieveAllText = true;

//div元素生成
for (var i = 0; i < divs.length; i++) {
    if (checkEleVisible($(divs[i]))) {
        var bg_img = $(divs[i]).css('background-image');

        //检查其background-image和background-color，判断是否为图片类型或纯背景颜色类型
        if (checkStrNotNull(bg_img)) {
            var objImg = {};
            generalProperty($(divs[i]), divs[i], objImg, 2, "Common_Image");
            var imgUrl = $(divs[i]).css('background-image');
            getImgProperty($(divs[i]), objImg, (imgUrl).substring(5, imgUrl.length - 2));
            getBgProperty($(divs[i]), objImg);
            data['all_tools']['Common_Image'][objImg['layer_id']] = objImg; //装载image类型数据

        } else if (checkBackground($(divs[i]))) {
            var objBg = {};
            generalProperty($(divs[i]), divs[i], objBg, 1, "Common_Background");
            getBgProperty($(divs[i]), objBg);
            data['all_tools']['Common_Background'][objBg['layer_id']] = objBg; //装载background类型数据
        }

        //检查是否包含的数据
        var text = getText($(divs[i]));
        if (text != '') {
            var objText = {};
            generalProperty($(divs[i]), divs[i], objText, 3, "Common_Text");
            getTextProperty($(divs[i]), objText, text);
            objText['text-editable'] = false;
            data['all_tools']['Common_Text'][objText['layer_id']] = objText; //装载text类型数据
        }
    }
}

//img元素生成
for (var i = 0; i < imgs.length; i++) {
    if (checkEleVisible($(imgs[i]))) {
        var objImg = {};
        generalProperty($(imgs[i]), imgs[i], objImg, 2, "Common_Image");

        //获取img的src
        var src = '';
        if (checkStrNotNull($(imgs[i]).attr('data-src'))) {
            src = $(imgs[i]).attr('data-src');
        } else {
            src = $(imgs[i]).attr('src');
        }

        getImgProperty($(imgs[i]), objImg, src);
        getBgProperty($(imgs[i]), objImg);
        data['all_tools']['Common_Image'][objImg['layer_id']] = objImg; //装载image类型数据
    }
}

//span元素生成
for (var i = 0; i < spans.length; i++) {
    if (checkEleVisible($(spans[i]))) {
        var text = getText($(spans[i]));
        if (text != '') {
            var objText = {};
            generalProperty($(spans[i]), spans[i], objText, 3, "Common_Text");
            getTextProperty($(spans[i]), objText, text);
            objText['text-editable'] = false;
            objText['width'] += 10; //组件添加完border后，会相应减10，需相应添加10
            data['all_tools']['Common_Text'][objText['layer_id']] = objText; //装载text类型数据
        }
    }
}

//button元素，分为submit和普通button两种
for (var i = 0; i < buttons.length; i++) {
    if (checkEleVisible($(buttons[i]))) {
        var text = getText($(buttons[i]));
        if (text != '') {
            var objBtn = {};
            if ($(buttons[i]).attr('type') == 'submit') {
                generalProperty($(buttons[i]), buttons[i], objBtn, 3, "Submit");
                getBgProperty($(buttons[i]), objBtn);
                getTextProperty($(buttons[i]), objBtn, text);
                getPaddingProperty($(buttons[i]), objBtn);
                setSubmitProperty(objBtn);
                objBtn['text_show'] = true;
                data['all_tools']['Submit'][objBtn['layer_id']] = objBtn; //装载Submit类型数据

            } else {
                generalProperty($(buttons[i]), buttons[i], objBtn, 3, "Common_Button");
                getBgProperty($(buttons[i]), objBtn);
                getTextProperty($(buttons[i]), objBtn, text);
                getPaddingProperty($(buttons[i]), objBtn);
                objBtn['text_show'] = true;
                data['all_tools']['Common_Button'][objBtn['layer_id']] = objBtn; //装载text类型数据
            }
        }
    }
}

//textarea元素
for (var i = 0; i < textareas.length; i++) {
    if (checkEleVisible($(textareas[i]))) {
        var objTextArea = {};
        generalProperty($(textareas[i]), textareas[i], objTextArea, 3, "TextArea");
        getBgProperty($(textareas[i]), objTextArea);
        getTextProperty($(textareas[i]), objTextArea, null);
        getPaddingProperty($(textareas[i]), objTextArea);
        if (checkStrNotNull($(textareas[i]).attr('placeholder'))) {
            objTextArea['placeholder'] = $(textareas[i]).attr('placeholder');
        }
        data['all_tools']['TextArea'][objTextArea['layer_id']] = objTextArea; //装载TextArea类型数据
    }
}

//input元素生成，type分别为：text, password, submit, button
for (var i = 0; i < inputs.length; i++) {
    if (checkEleVisible($(inputs[i]))) {
        var objInput = {};
        switch ($(inputs[i]).attr('type')) {
            case 'text': {
                generalProperty($(inputs[i]), inputs[i], objInput, 3, "TextInput");
                getBgProperty($(inputs[i]), objInput);
                getTextProperty($(inputs[i]), objInput, null);
                getPaddingProperty($(inputs[i]), objInput);
                if (checkStrNotNull($(inputs[i]).attr('placeholder'))) {
                    objInput['placeholder'] = $(inputs[i]).attr('placeholder');
                }
                data['all_tools']['TextInput'][objInput['layer_id']] = objInput; //装载TextInput类型数据
                break;
            }
            case 'password': {
                generalProperty($(inputs[i]), inputs[i], objInput, 3, "Password");
                getBgProperty($(inputs[i]), objInput);
                getTextProperty($(inputs[i]), objInput, null);
                getPaddingProperty($(inputs[i]), objInput);
                if (checkStrNotNull($(inputs[i]).attr('placeholder'))) {
                    objInput['placeholder'] = $(inputs[i]).attr('placeholder');
                }
                data['all_tools']['Password'][objInput['layer_id']] = objInput; //装载Password类型数据
                break;
            }
            case 'submit': {
                var text = $(inputs[i]).attr('value');
                if (text != '') {
                    generalProperty($(inputs[i]), inputs[i], objInput, 3, "Submit");
                    getBgProperty($(inputs[i]), objInput);
                    getTextProperty($(inputs[i]), objInput, text);
                    getPaddingProperty($(inputs[i]), objInput);
                    setSubmitProperty(objInput);
                    objInput['text_show'] = true;
                    data['all_tools']['Submit'][objInput['layer_id']] = objInput; //装载Submit类型数据
                }
                break;
            }
            case 'button': {
                var text = $(inputs[i]).attr('value');
                if (text != '') {
                    generalProperty($(inputs[i]), inputs[i], objInput, 3, "Common_Button");
                    getBgProperty($(inputs[i]), objInput);
                    getTextProperty($(inputs[i]), objInput, text);
                    getPaddingProperty($(inputs[i]), objInput);
                    objInput['text_show'] = true;
                    data['all_tools']['Common_Button'][objInput['layer_id']] = objInput; //装载Common_Button类型数据
                }
                break;
            }
            default: {
                break;
            }
        }
    }
}

function checkEleVisible(ele) {
    var rect = ele[0].getBoundingClientRect();
    if (ele.css('display') != 'none' && ele.css('visibility') != 'hidden' && (rect.width * rect.height) > 0) {
        return true;
    } else {
        return false;
    }
}

function checkStrNotNull(str) {
    if (str != 'none' && str != '' && str != undefined && str != null) {
        return true;
    }
}

//结合background-color和border-color，看是否需要生成background
function checkBackground(ele) {
    var bg_color = ele.css('background-color');
    var border = ele.css('border-width');
    if (bg_color != 'rgba(0, 0, 0, 0)' || border != '0px') {
        return true
    } else {
        return false;
    }
}

function generalProperty(ele, originEle, obj, rate, type) {
    obj['layer_id'] = num++;
    obj['layer_rate'] = rate;
    obj['type'] = type;
    obj['name'] = type + '_' + num;

    //获取元素的top, left, width, height, 确保left, top大于零
    var rect = originEle.getBoundingClientRect();
    var left = Math.round(rect.left + window.scrollX);
    var top = Math.round(rect.top + window.scrollY);
    obj['left'] = left > 0 ? left : 0;
    obj['top'] = top > 0 ? top : 0;

    var width = rect.width;
    obj['width'] = width > windowsWidth ? (windowsWidth - 10) : width; //留10个像素给border
    obj['height'] = rect.height;
    obj['show'] = true;
    obj['opacity'] = parseFloat(ele.css('opacity'));
}

function getImgProperty(ele, obj, src) {
    obj['image_reposition'] = false;
    //查看图片url是否http开头，如果是则直接使用，否则添加在url路径前面添加该网站的host
    if (src.startsWith('http')) {
        obj['src'] = src;
    } else {
        //获取host数据
        var host = $(location).attr('host');
        if (!host.startsWith('http')) {
            host = $(location).attr('protocol') + '//' + host;
        }
        obj['src'] = host + src;
    }

    obj['bg-position-left'] = parseInt(ele.css('background-position-x'));
    obj['bg-position-top'] = parseInt(ele.css('background-position-y'));
    obj['bg-repeat'] = ele.css('background-repeat');
    obj['bg-size'] = 101;
}

function getBgProperty(ele, obj) {
    obj['bg-color'] = ele.css('background-color');
    obj['border-color'] = ele.css('border-color');
    obj['border-top-width'] = parseInt(ele.css('border-top-width').replace('px', ''));
    obj['border-left-width'] = parseInt(ele.css('border-left-width').replace('px', ''));
    obj['border-right-width'] = parseInt(ele.css('border-right-width').replace('px', ''));
    obj['border-bottom-width'] = parseInt(ele.css('border-bottom-width').replace('px', ''));
    obj['border-top-left-radius'] = parseInt(ele.css('border-top-left-radius').replace('px', ''));
    obj['border-top-right-radius'] = parseInt(ele.css('border-top-right-radius').replace('px', ''));
    obj['border-bottom-left-radius'] = parseInt(ele.css('border-bottom-left-radius').replace('px', ''));
    obj['border-bottom-right-radius'] = parseInt(ele.css('border-bottom-right-radius').replace('px', ''));
}

function getTextProperty(ele, obj, text) {
    if (text != null) {
        obj['text'] = text;
        obj['text-length'] = text.length;
    }
    obj['font-size'] = parseInt(ele.css('font-size').replace('px', ''));
    obj['font-weight'] = parseInt(ele.css('font-weight'));
    obj['line-height'] = 150;
    obj['text-align'] = ele.css('text-align');
    obj['font-family'] = ele.css('font-family').replace(/"/g, '');
    obj['font-style'] = ele.css('font-style');
    obj['font-color'] = ele.css('color');
    obj['text-decoration'] = ele.css('text-decoration-line');
}

function getPaddingProperty(ele, obj) {
    obj['padding-left'] = parseInt(ele.css('padding-left').replace('px', ''));
    obj['padding-top'] = parseInt(ele.css('padding-top').replace('px', ''));
    obj['padding-right'] = parseInt(ele.css('padding-right').replace('px', ''));
    obj['padding-bottom'] = parseInt(ele.css('padding-bottom').replace('px', ''));
}


function setSubmitProperty(objInput) {
    objInput['text_show'] = true;
    objInput['widget_reposition'] = true;
    objInput['post_url'] = '#';
    objInput['post_param'] = {
        'TextInput': {},
        'Password': {},
        'TextArea': {},
        'CheckBox': {},
        'RadioBox': {},
        'SelectOptions': {},
        'DatePicker': {},
        'File': {},
    };
}

/**
 * 获取元素的text，如果元素有子元素则返回去除子元素后的文本
 * @param ele
 */
function getText(ele) {
    var text = '';
    if (ele.children().length > 0) {
        //设置一个开关，是否获取所有文本数据，false时只获取无child element的元素文本
        if (retrieveAllText) {
            text = ele.clone()	//clone the element
                .children()	//select all the children
                .remove()	//remove all the children
                .end()	//again go back to selected element
                .text() //get the text of element
                .trim();
        }
    } else {
        text = ele.text().trim();
    }
    return text;
}

return JSON.stringify(data);
//console.log('data', data);
