var totalEquipmentChartData={
    labels : ["Type"],
    datasets : [{
        label : "Equipment",
        data : [1]
    }
   ]
};

var windowEquipmentChartData={
    labels : ["Type"],
    datasets : [{
        label : "Equipment",
        data : [1]
    }
   ]
};


jQuery(document).ready(function() {
    //Charts
    var ctx1 = document.getElementById("totalEquipmentChart").getContext("2d");
    window.tChart = new Chart(ctx1, {
                type: 'bar',
                data: totalEquipmentChartData
            });

    var ctx2 = document.getElementById("windowEquipmentChart").getContext("2d");
    window.wChart = new Chart(ctx2, {
                type: 'bar',
                data: windowEquipmentChartData
            });

    //tables
    var totalEquipmentList = jQuery("#total_equipment");
    var windowEquipmentList = jQuery("#window_equipment");

    //use sockjs
    var socket = new SockJS('/stomp');
    var stompClient = Stomp.over(socket);

    stompClient.connect({ }, function(frame) {
        //subscribe "/topic/equipmentData" message
        stompClient.subscribe("/topic/equipmentData", function(data) {
            var dataList = data.body;
            var resp=jQuery.parseJSON(dataList);

            //Total equipment
            var totalOutput='';
            jQuery.each(resp.totalEquipment, function(i,vh) {
                 totalOutput +="<tbody><tr><td>"+ vh.equipmentId+"</td><td>"+vh.sensorType+"</td><td>"+vh.totalCount+"</td><td>"+(vh.totalSum/vh.totalCount).toFixed(2)+"</td><td>"+vh.timeStamp+"</td></tr></tbody>";
            });
            var t_tabl_start = "<table class='table table-bordered table-condensed table-hover innerTable'><thead><tr><th>Equipment</th><th>Type</th><th>Count</th><th>Average</th><th>Time</th></tr></thead>";
            var t_tabl_end = "</table>";
            totalEquipmentList.html(t_tabl_start+totalOutput+t_tabl_end);

            //Window equipment
            var windowOutput='';
            jQuery.each(resp.windowEquipment, function(i,vh) {
                 windowOutput +="<tbody><tr><td>"+ vh.equipmentId+"</td><td>"+vh.sensorType+"</td><td>"+vh.totalCount+"</td><td>"+(vh.totalSum/vh.totalCount).toFixed(2)+"</td><td>"+vh.timeStamp+"</td></tr></tbody>";
            });
            var w_tabl_start = "<table class='table table-bordered table-condensed table-hover innerTable'><thead><tr><th>Equipment</th><th>Type</th><th>Count</th><th>Average</th><th>Time</th></tr></thead>";
            var w_tabl_end = "</table>";
            windowEquipmentList.html(w_tabl_start+windowOutput+w_tabl_end);

            //draw total equipment chart
            drawBarChart(resp.totalEquipment,totalEquipmentChartData);
            window.tChart.update();

            //draw window equipment chart
            drawBarChart(resp.windowEquipment,windowEquipmentChartData);
            window.wChart.update();

        });
    });
});

function drawBarChart(equipmentDetail,equipmentChartData){
    //Prepare data for equipment chart
    var chartLabel = ["Temperature", "Current", "Voltage", "Vibration", "Level"];
    var equipmentName = ["Equipment_A", "Equipment_B", "Equipment_C"];
    var chartData0 =[0,0,0,0,0], chartData1 =[0,0,0,0,0], chartData2 =[0,0,0,0,0];

    jQuery.each(equipmentDetail, function(i,vh) {

        if(vh.equipmentId == equipmentName[0]){
            chartData0.splice(chartLabel.indexOf(vh.sensorType),1,(vh.totalSum/vh.totalCount).toFixed(2));
        }
        if(vh.equipmentId == equipmentName[1]){
            chartData1.splice(chartLabel.indexOf(vh.sensorType),1,(vh.totalSum/vh.totalCount).toFixed(2));
        }
        if(vh.equipmentId == equipmentName[2]){
            chartData2.splice(chartLabel.indexOf(vh.sensorType),1,(vh.totalSum/vh.totalCount).toFixed(2));
        }
    });

    var equipmentData = {
        labels : chartLabel,
        datasets : [{
            label				  : equipmentName[0],
            borderColor           : "#878BB6",
            backgroundColor       : "#878BB6",
            data                  : chartData0
        },
        {
            label				  : equipmentName[1],
            borderColor           : "#4ACAB4",
            backgroundColor       : "#4ACAB4",
            data                  : chartData1
        },
        {
            label				  : equipmentName[2],
            borderColor           : "#FFEA88",
            backgroundColor       : "#FFEA88",
            data                  : chartData2
        }

        ]
    };
    //update chart
    equipmentChartData.datasets=equipmentData.datasets;
    equipmentChartData.labels=equipmentData.labels;
 }

 function getRandomColor() {
    return  'rgba(' + Math.round(Math.random() * 255) + ',' + Math.round(Math.random() * 255) + ',' + Math.round(Math.random() * 255) + ',' + ('1') + ')';
};

