package ru.aten.telegram_bot.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.model.Schedule;
import ru.aten.telegram_bot.repository.ScheduleRepository;

@Service
@AllArgsConstructor
public class ScheduleService {

    // private final ScheduleRepository scheduleRepository;

    // public void saveSchedulesFromExcel(InputStream inputStream) throws Exception  {
    //     List<Schedule> schedules = parseExcelFile(inputStream);
    // }

    // private List<Schedule> parseExcelFile(InputStream inputStream) throws Exception {
    //     List<Schedule> schedules = new ArrayList<>();
    //     Workbook workbook = new HSSFWorkbook();

    //     Sheet sheet = workbook.getSheetAt(0);

    //     for (Row row : sheet) {
    //         Schedule schedule = Schedule.builder()
    //                 .build();
    //     }


    //     return schedules;
    // }

}
