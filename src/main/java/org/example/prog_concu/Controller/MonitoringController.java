package org.example.prog_concu.Controller;

//import com.healthmonitor.service.MonitoringManager;
import org.example.prog_concu.Service.MonitoringManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MonitoringManager monitoringManager;

    @Autowired
    public MonitoringController(MonitoringManager monitoringManager) {
        this.monitoringManager = monitoringManager;
    }

    @GetMapping
    public String viewMonitoring(Model model) {
        model.addAttribute("sensorData", monitoringManager.getAllSensorData());
        return "monitoring";
    }

    @PostMapping("/start")
    public String startMonitoring(@RequestParam Long patientId) {
        monitoringManager.startMonitoring(patientId);
        return "redirect:/patients";
    }
}
