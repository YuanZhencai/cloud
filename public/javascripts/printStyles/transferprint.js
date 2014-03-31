 var LODOP;//全局的
//打印
function printPickData(data){
LODOP.PRINT_INIT("warehouse shipped");
var pagewith=210;
var pagelegth=297;
LODOP.SET_PRINT_STYLE("FontSize",9);
var leftLength = 2;
var Top1 = Top2 = 3;
var Left1 = 2;
var oneCol = 32+Left1;
var twoCol = 22+oneCol;
var threeCol = twoCol+40;
var fourCol = threeCol+21;
var fiveCol = fourCol+39;
var sixCol = fiveCol+20;
var sevenCol = sixCol+12;
var eightCol = sevenCol+12;
var high = 7;
//alert(data.length);
var len = data.length;
if(len <= 37){
	LODOP.ADD_PRINT_LINE(Top1+'mm',Left1+'mm', Top2+'mm', pagewith-10+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+'mm',Left1+'mm', Top2+high+'mm', Left1+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+'mm',oneCol+'mm', Top2+high+'mm', oneCol+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+'mm',twoCol+'mm', Top2+high+'mm', twoCol+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+'mm',threeCol+'mm', Top2+high+'mm', threeCol+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+'mm',fourCol+'mm', Top2+high+'mm', fourCol+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+'mm',fiveCol+'mm', Top2+high+'mm', fiveCol+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+'mm',sixCol+'mm', Top2+high+'mm', sixCol+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+'mm',sevenCol+'mm', Top2+high+'mm', sevenCol+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+'mm',eightCol+'mm', Top2+high+'mm', eightCol+'mm', 0, 1);
	LODOP.ADD_PRINT_LINE(Top1+high+'mm',Left1+'mm', Top2+high+'mm', pagewith-10+'mm', 0, 1);
	LODOP.ADD_PRINT_TEXTA("L", 1+Top1+'mm', 1+Left1+'mm', oneCol+'mm', high+'mm', "PI No");
	LODOP.ADD_PRINT_TEXTA("L", 1+Top1+'mm', 1+oneCol+'mm', twoCol+'mm', high+'mm', "Batch No");
	LODOP.ADD_PRINT_TEXTA("L", 1+Top1+'mm', 1+twoCol+'mm', threeCol+'mm', high+'mm', "From Area");
	LODOP.ADD_PRINT_TEXTA("L", 1+Top1+'mm', 1+threeCol+'mm', fourCol+'mm', high+'mm', "From Bin");
	LODOP.ADD_PRINT_TEXTA("L", 1+Top1+'mm', 1+fourCol+'mm', fiveCol+'mm', high+'mm', "To Area");
	LODOP.ADD_PRINT_TEXTA("L", 1+Top1+'mm', 1+fiveCol+'mm', sixCol+'mm', high+'mm', "To Bin");
	LODOP.ADD_PRINT_TEXTA("L", 1+Top1+'mm', 1+sixCol+'mm', sevenCol+20+'mm', high+'mm', "Qty");
	LODOP.ADD_PRINT_TEXTA("L", 1+Top1+'mm', 1+sevenCol+'mm', eightCol+'mm', high+'mm', "Total");
	for(i = 0; i < len; i++){
		LODOP.ADD_PRINT_LINE(Top1+'mm',Left1+'mm', Top2+high*(2+i)+'mm', Left1+'mm', 0, 1);
		LODOP.ADD_PRINT_LINE(Top1+'mm',oneCol+'mm', Top2+high*(2+i)+'mm', oneCol+'mm', 0, 1);
		LODOP.ADD_PRINT_LINE(Top1+'mm',twoCol+'mm', Top2+high*(2+i)+'mm', twoCol+'mm', 0, 1);
		LODOP.ADD_PRINT_LINE(Top1+'mm',threeCol+'mm', Top2+high*(2+i)+'mm', threeCol+'mm', 0, 1);
		LODOP.ADD_PRINT_LINE(Top1+'mm',fourCol+'mm', Top2+high*(2+i)+'mm', fourCol+'mm', 0, 1);
		LODOP.ADD_PRINT_LINE(Top1+'mm',fiveCol+'mm', Top2+high*(2+i)+'mm', fiveCol+'mm', 0, 1);
		LODOP.ADD_PRINT_LINE(Top1+'mm',sixCol+'mm', Top2+high*(2+i)+'mm', sixCol+'mm', 0, 1);
		LODOP.ADD_PRINT_LINE(Top1+'mm',sevenCol+'mm', Top2+high*(2+i)+'mm', sevenCol+'mm', 0, 1);
		LODOP.ADD_PRINT_LINE(Top1+'mm',eightCol+'mm', Top2+high*(2+i)+'mm', eightCol+'mm', 0, 1);
		LODOP.ADD_PRINT_LINE(Top1+high*(2+i)+'mm',Left1+'mm', Top2+high*(2+i)+'mm', pagewith-10+'mm', 0, 1);
		LODOP.ADD_PRINT_TEXTA("D", 1+Top1+high*(1+i)+'mm', 1+Left1+'mm', oneCol+'mm', high*(2+i)+'mm', data[i].piNo);
		LODOP.ADD_PRINT_TEXTA("D", 1+Top1+high*(1+i)+'mm', 1+oneCol+'mm', twoCol+'mm', high*(2+i)+'mm', data[i].batchNo);
		LODOP.ADD_PRINT_TEXTA("D", 1+Top1+high*(1+i)+'mm', 1+twoCol+'mm', threeCol+'mm', high*(2+i)+'mm', data[i].fromArea);
		LODOP.ADD_PRINT_TEXTA("D", 1+Top1+high*(1+i)+'mm', 1+threeCol+'mm', fourCol+'mm', high*(2+i)+'mm', data[i].fromBin);
		LODOP.ADD_PRINT_TEXTA("D", 1+Top1+high*(1+i)+'mm', 1+fourCol+'mm', fiveCol+'mm', high*(2+i)+'mm', data[i].toArea);
		LODOP.ADD_PRINT_TEXTA("D", 1+Top1+high*(1+i)+'mm', 1+fiveCol+'mm', sixCol+'mm', high*(2+i)+'mm', data[i].toBin);
		LODOP.ADD_PRINT_TEXTA("D", 1+Top1+high*(1+i)+'mm', 1+sixCol+'mm', sevenCol+20+'mm', high*(2+i)+'mm', data[i].qty);
		LODOP.ADD_PRINT_TEXTA("D", 1+Top1+high*(1+i)+'mm', 1+sevenCol+'mm', eightCol+'mm', high*(2+i)+'mm', data[i].sum);
	}
}
LODOP.SET_PRINT_STYLEA("L","FontName","微软雅黑");
LODOP.SET_PRINT_STYLEA("L","FontSize",12);
LODOP.SET_PRINT_STYLEA("L","Bold",1);
LODOP.SET_PRINT_STYLEA("D","FontName","微软雅黑");
LODOP.SET_PRINT_STYLEA("D","FontSize",11);
//printdates(topHeight,leftLength,Width,data);
//LODOP.PRINT_DESIGN(); 
LODOP.PREVIEW();
//LODOP.PRINT();
}
