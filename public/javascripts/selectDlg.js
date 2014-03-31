var D_REGEXP =/\d+/;
var W_REGEXP =/\-|[a-zA-Z]+/;
/*
 * 
 */
function  splitStrToArr(str){
	if(isEmpty(str)){
		return null;
	}
	var str1 = str.match(D_REGEXP);
	var str2 = str.match(W_REGEXP);
	var arr = new Array();
	if(str2==null){
		str2= new Array();
		str2.push("#");
	}
	var temp  = parseFloat(str1[0]);
	arr.push(temp);
	arr.push(str2[0]);
	return arr;
}
function isPalletNo(str){
	if(isEmpty(str)){
		return true;
	}
	var str1 = str.match(D_REGEXP);
	if(str1 == null){
		return false;
	}else{
		return true;
	}
}
/*
 * 如果字符串不为空返回true
 */
function isNotEmpty(str){
	if(str != null && str != undefined && str != ""){
		return true;
	}else{
		return false;
	}
}
/*
 *  如果字符串为空返回true
 */
function isEmpty(str){
	return !isNotEmpty(str);
}
/*
 *  如果数组不为空返回true
 */
function isNotEmptyArr(arr){
	if(arr != null && arr != undefined && arr.length != 0){
		return true;
	} else {
		return false;
	}
}
/*
 *  如果数组为空返回true
 */
function isEmptyArr(arr){
	return !isNotEmptyArr(arr)
}
/*
 * 根据select对话框挣输入的pallet的no 和 要选的数组，选出需要的元素
 */
function baseFromToSelectQty(selectVo, arr){
	try{
		if (!isNotEmpty(selectVo.from) && !isNotEmpty(selectVo.to)) {
		    alert("Please Enter at least one Pallet No");
			return false;
		}
		if(!isPalletNo(selectVo.from) || !isPalletNo(selectVo.to)){
			 alert("Enter the correct Pallet No");
			 return false;
		}
		if(arr != undefined){
			for(var i = 0, len = arr.length; i < len; i++){
				arr[i].ifSelect=false;
			}
		}
		var fromArr = splitStrToArr(selectVo.from);
		var toArr =  splitStrToArr(selectVo.to);
		if(isNotEmpty(selectVo.from) && isNotEmpty(selectVo.to) && fromArr[1]!=toArr[1]){
			 alert("from Pallet No and to Pallet No must have same word([a-zA-Z])");
			 return false;
		}
		if(isNotEmpty(selectVo.from) && isNotEmpty(selectVo.to) && fromArr[0]>=toArr[0]){
			 alert("from Pallet No digit must less than to Pallet No  digit([0-9])");
			 return false;
		}
		if(isEmpty(selectVo.from) && isNotEmpty(selectVo.to)) {
			for ( var i = 0, len = arr.length; i < len; i++) {
				var tempArr = splitStrToArr(arr[i].stockno);
				if(tempArr[1] == toArr[1] && tempArr[0] <= toArr[0] && arr[i].ifExecution == false){
					arr[i].ifSelect = true;
				}
			}
		}
		if (isNotEmpty(selectVo.from) && isEmpty(selectVo.to)) {
			for ( var i = 0, len = arr.length; i < len; i++) {
				var tempArr = splitStrToArr(arr[i].stockno);
				if(tempArr[1] == fromArr[1] && tempArr[0] >= fromArr[0] && arr[i].ifExecution == false){
					arr[i].ifSelect = true;
				}
			}
		}
		if (isNotEmpty(selectVo.from) && isNotEmpty(selectVo.to)) {
			for ( var i = 0, len = arr.length; i < len; i++) {
				var tempArr = splitStrToArr(arr[i].stockno);
				if(tempArr[1] == fromArr[1] && tempArr[0] >= fromArr[0] && tempArr[0] <= toArr[0] && arr[i].ifExecution == false){
					arr[i].ifSelect = true;
				}
			}
		}
	}catch(err){
		console.log();
		return true;
	}
	return true
}
/**
 * 计算no of pallet和SKU
 * @param
 */
function calculateNo(selectVo, arr){
	var doCalculate = false;
	var fromArr = splitStrToArr(selectVo.from);
	var toArr =  splitStrToArr(selectVo.to);
	if(isNotEmpty(selectVo.from) && isNotEmpty(selectVo.to) && fromArr[1] == toArr[1] && fromArr[0] <= toArr[0]){
		doCalculate = true;
	}
	if(doCalculate){
		var number = 0;
		var allSku = 0;
		for(var i = 0, len = arr.length; i<len;i++){
			if(arr[i].ifExecution == false){
				var tempArr = splitStrToArr(arr[i].stockno);
				if(tempArr[1] == fromArr[1] && tempArr[0] >= fromArr[0] && tempArr[0] <= toArr[0]){
					number = Number(number) + Number(1);
					allSku = Number(allSku) + Number(arr[i].palletQty);
				}
			}
		}
		selectVo.number = Number(number);
		selectVo.palletSku = Number(allSku).toFixed(2);
	} else {
		selectVo.number = null;
		selectVo.palletSku = null;
	}
}