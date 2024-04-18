package io.dataease.feign.client;

import io.dataease.dto.UserGroupInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "sys-mgt")
public interface SysMgtClient {

    /**
     * 用户详情查询
     * @param userIds
     * @return
     * {
     *     "d164d704854c48cd9a940c3a5ee36f9d": {
     *         "roleName": "超级管理员",
     *         "id": "d164d704854c48cd9a940c3a5ee36f9d",
     *         "userName": "superAdmin"
     *     }
     * }
     */
    @RequestMapping(value="/sys-mgt/user/getUserNames", method= RequestMethod.POST)
    Map<String, Map<String, String>> getUserNames(@RequestBody List<String> userIds,
                                                  @RequestHeader(name = "Authorization",required = true) String Authorization);


    @GetMapping("/sys-mgt/user/getMenuAndRoles")
    Map<String, Map<String, Object>> getMenuAndRoles(@RequestHeader(name = "Authorization",required = true) String Authorization);
    /**
     * 用户所属部门查询
     * @param userIds
     * @return
     * {
     *   "4e8845ce0380481a963f0b6b2b5821d2": {
     *     "userGroupId": "1cf3fd11ed9b40f2bbf5812fd6154756",
     *     "userGroupName": "市场一部",
     *     "userId": "4e8845ce0380481a963f0b6b2b5821d2"
     *   },
     *   "d164d704854c48cd9a940c3a5ee36f9d": {
     *     "userGroupId": "1cf3fd11ed9b40f2bbf5812fd6154756",
     *     "userGroupName": "市场一部",
     *     "userId": "d164d704854c48cd9a940c3a5ee36f9d"
     *   }
     * }
     */
    @RequestMapping(value="/sys-mgt/user/getOrganizationInfos",method=RequestMethod.POST)
    Map<String, Map<String, String>> getOrganizationInfos(@RequestBody List<String> userIds,
                                                          @RequestHeader(name = "Authorization",required = true) String Authorization);

    /**
     * 部门列表查询
     * @return
     */
    @RequestMapping(value="/sys-mgt/userGroup/getUserGroupTree",method=RequestMethod.GET)
    List<UserGroupInfoDto> getUserGroupTree(@RequestHeader(name = "Authorization",required = true) String Authorization);

    /**
     * 部门用户查询
     * @param userGroupId
     * @return
     */
    @RequestMapping(value="/sys-mgt/user/getUsersByGroupId",method=RequestMethod.POST)
    List<Map<String, String>> getUsersByGroupId(@RequestBody String userGroupId,
                                                @RequestHeader(name = "Authorization",required = true) String Authorization);
    @PostMapping(value = "/sys-mgt/userGroup/getUserGroupNameById")
    Map<String, String> getUserGroupNameById(@RequestBody String userGroupId,
                                @RequestHeader(name = "Authorization",required = true) String Authorization);

}
