//控制层 
app.controller('seckillGoodsController' ,function($scope,$location,$http,seckillGoodsService){	
	 //读取列表数据绑定到表单中  
	$scope.findList=function(){
		seckillGoodsService.findList().success(
			function(response){
				$scope.list=response;
			}			
		);
	}     
	//查询实体 
	$scope.findOne=function(){	
		seckillGoodsService.findOne($location.search()['id']).success(
			function(response){
				$scope.entity= response;
			}
		);				
	}
	
	//提交订单
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
			function(response){
				if(response.success){
					alert("下单成功，请在1分钟内完成支付");
					location.href="pay.html";
				}else{
					alert(response.message);
				}
			}
		);		
	}

	

});	
