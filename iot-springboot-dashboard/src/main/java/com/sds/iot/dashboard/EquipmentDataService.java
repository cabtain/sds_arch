package com.sds.iot.dashboard;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.sds.iot.dao.TotalEquipmentDataRepository;
import com.sds.iot.dao.entity.TotalEquipmentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sds.iot.dao.WindowEquipmentDataRepository;
import com.sds.iot.dao.entity.WindowEquipmentData;

/**
 * Service class to send equipment data messages to dashboard ui at fixed interval using web-socket.
 */
@Service
public class EquipmentDataService {
    private static final Logger logger = Logger.getLogger(EquipmentDataService.class.getName());

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private TotalEquipmentDataRepository totalRepository;

    @Autowired
    private WindowEquipmentDataRepository windowRepository;

    private static DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    //Method sends equipment data message in every 5 seconds.
    @Scheduled(fixedRate = 5000)
    public void trigger() {
        List<TotalEquipmentData> totalEquipmentList = new ArrayList<>();
        List<WindowEquipmentData> windowEquipmentList = new ArrayList<>();
        //Call dao methods
        totalRepository.findEquipmentDataByDate(sdf.format(new Date())).forEach(e -> totalEquipmentList.add(e));
        windowRepository.findEquipmentDataByDate(sdf.format(new Date())).forEach(e -> windowEquipmentList.add(e));
        //prepare response
        Response response = new Response();
        response.setTotalEquipment(totalEquipmentList);
        response.setWindowEquipment(windowEquipmentList);
        logger.info("Sending to UI " + response);
        //send to ui
        this.template.convertAndSend("/topic/equipmentData", response);
    }

}
