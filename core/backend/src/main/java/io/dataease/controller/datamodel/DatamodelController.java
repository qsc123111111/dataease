package io.dataease.controller.datamodel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "数据集：数据集组")
@ApiSupport(order = 40)
@RestController
@RequestMapping("dataset/group")
public class DatamodelController {
}
