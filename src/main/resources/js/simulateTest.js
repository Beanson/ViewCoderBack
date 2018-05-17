//装载数据的容器
var imgs = $('img'); //获取所有image组件
var data={
    'Common_Image':{},
};
var num = 0;

//img元素生成
for (var i = 0; i < imgs.length; i++) {
    if (checkEleVisible($(imgs[i]))) {
        var objImg = {};
        generalProperty($(imgs[i]), imgs[i], objImg, 2, "Common_Image");
        getImgProperty($(imgs[i]), imgs[i], objImg, $(imgs[i]).attr('src'));
        getBgProperty($(imgs[i]), objImg);
        data['Common_Image'][objImg['layer_id']] = objImg; //装载image类型数据
    }
}

return JSON.stringify(data);
//console.log('data', data);

function checkEleVisible(ele) {
    if (ele.css('display') != 'none' && ele.css('visibility') != 'hidden' && ele.width() * ele.height() > 0) {
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
    var rect = originEle.getBoundingClientRect();
    obj['left'] = Math.round(rect.left + window.scrollX);
    obj['top'] = Math.round(rect.top + window.scrollY);
    obj['width'] = rect.width;
    obj['height'] = rect.height;
    obj['show'] = true;
    obj['opacity'] = parseFloat(ele.css('opacity'));
}

function getImgProperty(ele, originEle, obj, src) {
    obj['image_reposition'] = false;
    //查看图片url是否http开头，如果是则直接使用，否则添加在url路径前面添加该网站的host
    if (src.startsWith('http')) {
        obj['src'] = src;
    } else {
        obj['src'] = $(location).attr('host') + src;
    }

    obj['ele'] = ele.get(0).outerHTML;
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
    obj['font-family'] = ele.css('font-family');
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
        'File': {}
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