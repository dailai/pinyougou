app.controller('orderInfoController',function($scope,addressService,cartService){
	
	/**
	 * 查询地址列表
	 */
	$scope.findAddressList=function(){
		addressService.findListByLoginUser().success(
				function(response){
					$scope.addressList=response;
					for(var i=0;i<$scope.addressList.length;i++){
						if($scope.addressList[i].isDefault=='1'){
							$scope.address=$scope.addressList[i];
							break;
						}
					}
					
					if($scope.address==null||$scope.address==''){
						$scope.address=$scope.addressList[0];
					}
				}
		);
	}
	
	/**
	 * 选择地址
	 */
	$scope.selectAddress=function(address){
		$scope.address=address;
	}
	
	/**
	 * 判断是否当前选中的地址
	 */
	$scope.isAddressSelected=function(address){
		if (address==$scope.address) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 支付方式选择
	 */
	$scope.order={paymentType:'1'};
	$scope.selectPayType=function(type){
		$scope.order.paymentType=type;
	}
	
	/**
	 * 查询购物车列表
	 */
	$scope.findCartList=function(){
		cartService.findCartList().success(
				function(response){
					$scope.cartList=response;
					sum();
				}
		);
	}
	
	
	/**
	 * 求和
	 */
	sum=function(){
		$scope.totalNum=cartService.sum($scope.cartList).totalNum;
		$scope.totalMoney=cartService.sum($scope.cartList).totalMoney;
	}
	
	//提交订单
	$scope.submitOrder=function(){
		$scope.order.receiverAreaName=$scope.address.address;//地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机号
		$scope.order.receiver=$scope.address.contact;//联系人
		cartService.submitOrder($scope.order).success(
			function(response){
				//如果是微信支付
				if(response.success  && $scope.order.paymentType=='1' ){
					location.href="pay.html";
				}else{
					alert(response.message);
				}				
			}
		);		
	}
	
	$scope.addressEntity={};
	//保存 地址
	$scope.saveAddress=function(){	
		//alert($scope.addressEntity.id);
		var serviceObject;//服务层对象  				
		if($scope.addressEntity.id!=null){//如果有ID
			serviceObject=addressService.update( $scope.addressEntity ); //修改  
		}else{
			serviceObject=addressService.add( $scope.addressEntity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.findAddressList();//重新加载;
		        	//location.reload();
				}else{
					alert(response.message);
				}
			}		
		);				
	}

	//修改地址回显
	$scope.findAddress=function(id){
		addressService.findOne(id).success(
			function(response){
				$scope.addressEntity= response;
		});
	}
	
	$scope.selectIds=[];
	//批量删除 
	$scope.dele=function(id){	
		$scope.selectIds.push(id);
		//获取选中的复选框			
		addressService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					$scope.findAddressList();//刷新列表
				}						
			}		
		);				
	}
});