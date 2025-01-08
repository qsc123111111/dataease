package io.dataease.job.sechedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.dataease.commons.utils.LogUtil;
import io.dataease.service.CleaningRebotService;
import io.dataease.service.datasource.DatasourceGroupService;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.kettle.KettleService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Schedular {
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DatasourceGroupService datasourceGroupService;
    @Resource
    private KettleService kettleService;

    @Resource
    private CleaningRebotService cleaningRebotService;

    @QuartzScheduled(cron = "0 0/3 * * * ?")
    public void updateDatasetTableStatus() {
        dataSetTableService.updateDatasetTableStatus();
    }

    @QuartzScheduled(cron = "0 0/3 * * * ?")
    public void updateDatasourceStatus() {
        datasourceService.checkDatasourceJob();
    }

    @QuartzScheduled(cron = "0 0/30 * * * ?")
    public void updateKettleStatus() {
        kettleService.updateKettleStatus();
    }

    @QuartzScheduled(cron = "0 0 0 ? * *")
    public void cheanDisusedData() {
        LogUtil.info("start execute clean task...");
        cleaningRebotService.execute();
    }

    // TODO 0 */5 * * * ?
    @QuartzScheduled(cron = "0 */1 * * * ?")
    public void fullScan() {
        LogUtil.info("start execute full-scan task...");
        datasourceGroupService.fullScan();
    }

}
