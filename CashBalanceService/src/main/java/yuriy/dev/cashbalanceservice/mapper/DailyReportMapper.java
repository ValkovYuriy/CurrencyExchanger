package yuriy.dev.cashbalanceservice.mapper;


import org.mapstruct.Mapper;
import yuriy.dev.cashbalanceservice.dto.DailyReportDto;
import yuriy.dev.cashbalanceservice.model.DailyReport;

@Mapper(componentModel = "spring")
public interface DailyReportMapper {

    DailyReport toDailyReport(DailyReportDto dailyReportDto);

    DailyReportDto toDailyReportDto(DailyReport dailyReport);
}
