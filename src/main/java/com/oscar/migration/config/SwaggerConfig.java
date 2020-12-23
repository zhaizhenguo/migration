package com.oscar.migration.config;import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.*;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/**
 author: Administrator
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("mybatis-plus and swagger2")
				.description("mybatis-plus and swagger2 结合使用")
				.termsOfServiceUrl("http://localhost:8081/")
				.version("1.0.0")
				.build();
	}


	@Bean
	public Docket createRestApi() {
		//可以控制 哪些符合条件的 接口 对外暴露文档；
		Predicate<RequestHandler> predicate = (input) -> {
			Set<String> patterns = input.getRequestMapping().getPatternsCondition().getPatterns();
			for (String cur : patterns) {
				//if (cur.startsWith("/api")) return true;
			}
			return true;
		};

		ResponseMessage responseMesssageSucc = new ResponseMessageBuilder()
				.code(0)
				.message("处理成功")
				.build();
		ResponseMessage responseMesssageFail = new ResponseMessageBuilder()
				.code(-1)
				.message("处理失败")
				.build();
		List<ResponseMessage> list = new ArrayList();
		list.add(responseMesssageSucc);
		list.add(responseMesssageFail);

		// 添加请求参数，我们这里把token作为请求头部参数传入后端
		ParameterBuilder parameterBuilder = new ParameterBuilder();
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameterBuilder.name("token")
				.description("令牌")
				.modelRef(new ModelRef("string"))
				.parameterType("header")
				.required(false)
				.build();
		parameters.add(parameterBuilder.build());
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any())
				.build()
				.globalOperationParameters(parameters);

//		return new Docket(DocumentationType.SWAGGER_2)
//				.useDefaultResponseMessages(false)
//				.globalResponseMessage(RequestMethod.POST, list)
//				.apiInfo(apiInfo())
//				.select()
//				.apis(predicate)
//				.apis(RequestHandlerSelectors.basePackage("com.oscar.migration.controller"))
//				.paths(PathSelectors.any())
//				.build();
	}

}