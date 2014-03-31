var LODOP;//全局的
//打印
function printPickData(data){
LODOP.PRINT_INITA(0,0,384,192,"warehouse shipped");
LODOP.SET_PRINT_PAGESIZE(3,1016,508,"A4");
LODOP.SET_PRINT_STYLE("PenStyle",10);
LODOP.SET_PRINT_STYLE("FontSize",10);
var qrcdeWith = 50;
var top = 0;
var topPadding = 5;
var left = 49.2;
var textHeight = 7.4;

/*LODOP.ADD_PRINT_BARCODE('0.8mm','0.5mm',qrcdeWith+'mm',qrcdeWith+'mm',"QRCode","stock@@"+data.extMap.piNo+'||'+data.extMap.lotNo+'||'+data.extMap.stockNo);

LODOP.ADD_PRINT_TEXTA("L", topPadding+'mm', left+'mm', '15.9mm', textHeight+'mm', "PI No:");

LODOP.ADD_PRINT_TEXTA("D", topPadding-1+'mm', '62.9mm', 68+'mm', 10.1+'mm', data.extMap.piNo);

LODOP.ADD_PRINT_TEXTA("L", (topPadding*3)+'mm', left+'mm', '23.8mm', textHeight+'mm', "Batch No:");

LODOP.ADD_PRINT_TEXTA("D", (topPadding*3)-1+'mm', '71mm', 68+'mm',10.1+'mm', data.extMap.lotNo);

LODOP.ADD_PRINT_TEXTA("L", (topPadding*5)+'mm', left+'mm', '24.6mm', textHeight+'mm', "Pallet No:");

LODOP.ADD_PRINT_TEXTA("D", (topPadding*5)-1+'mm', '71mm', 68+'mm', 10.1+'mm', data.extMap.stockNo);

LODOP.ADD_PRINT_TEXTA("L", (topPadding*7)+'mm', left+'mm', '28.6mm', textHeight+'mm', "Qty/Pallet:");

LODOP.ADD_PRINT_TEXTA("D",(topPadding*7)-1+'mm', '75mm',68+'mm',10.1+'mm', data.extMap.qty+" "+data.uomCode);
*/
//top left width height 
LODOP.ADD_PRINT_BARCODE("0.8mm","0.5mm","50mm","50mm","QRCode","stock@@"+data.piNo+'||'+data.batchNo+'||'+data.palletNo);
/*LODOP.ADD_PRINT_TEXTA("L","5mm","49.2mm","15.9mm","7.4mm","PI No:");
LODOP.ADD_PRINT_TEXTA("D","4mm","63mm","68mm","10.1mm", "071/XIII/OP/PI/2013");
LODOP.ADD_PRINT_TEXTA("L","15.1mm","49.2mm","23.8mm","7.4mm","Batch No:");
LODOP.ADD_PRINT_TEXTA("D","14mm","70.9mm","68mm","10.1mm",data.extMap.lotNo);
LODOP.ADD_PRINT_TEXTA("L","24.9mm","49.2mm","24.6mm","7.4mm","Pallet No:");
LODOP.ADD_PRINT_TEXTA("D","24.1mm","70.9mm","68mm","10.1mm",data.extMap.stockNo);
LODOP.ADD_PRINT_TEXTA("L","34.9mm","49.2mm","28.6mm","7.4mm","Qty/Pallet:");
LODOP.ADD_PRINT_TEXTA("D","34.1mm","74.9mm","68mm","10.1mm",data.extMap.qty+" "+data.uomCode);*/

LODOP.ADD_PRINT_TEXTA("L","5mm","48.4mm","17.2mm","7.4mm","PI No:");
LODOP.ADD_PRINT_TEXTA("D","0.8mm","65.6mm","33.6mm","18mm",data.piNo);
LODOP.ADD_PRINT_TEXTA("L","16.4mm","48.2mm","25.4mm","7.4mm","Batch No:");
LODOP.ADD_PRINT_TEXTA("D","14.8mm","73.6mm","25.9mm","10.1mm",data.batchNo);
LODOP.ADD_PRINT_TEXTA("L","27.5mm","48.7mm","24.6mm","7.4mm","Pallet No:");
LODOP.ADD_PRINT_TEXTA("D","26.7mm","73mm","26.7mm","10.1mm",data.palletNo);
LODOP.ADD_PRINT_TEXTA("L","38.4mm","48.4mm","25.7mm","7.4mm","Qty/Pallet:");
LODOP.ADD_PRINT_TEXTA("D","37.8mm","73.3mm","26.2mm","10.1mm",data.qty);

LODOP.SET_PRINT_STYLEA(0,"ShowBarText",0);

LODOP.SET_PRINT_STYLEA("All","QRCodeVersion",3);
LODOP.SET_PRINT_STYLEA("L","FontName","微软雅黑");
LODOP.SET_PRINT_STYLEA("L","FontSize",12);
LODOP.SET_PRINT_STYLEA("L","Bold",1);
LODOP.SET_PRINT_STYLEA("D","FontName","微软雅黑");
LODOP.SET_PRINT_STYLEA("D","FontSize",16);
//printdates(topHeight,leftLength,Width,data);
//LODOP.PRINT_DESIGN(); 
//LODOP.PREVIEW();
LODOP.PRINT();
}