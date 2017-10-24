//控制层 
app.controller('cartController', function($scope, cartService,loginService) {
	//保存 
	$scope.findCartList = function() {
		cartService.findCartList().success(
			function(response) {
					$scope.cartList=response;
					sum();
			});
		}
	
	//添加商品到购物车
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
				function(response){
					if (response.success) {
						$scope.findCartList();
					} else {
						alert(resonse.message);
					}
				}
		);
	}
	
	sum=function(){
		$scope.totalNum=cartService.sum($scope.cartList).totalNum;
		$scope.totalMoney=cartService.sum($scope.cartList).totalMoney;
	}
	
	//得到用户名 
	$scope.getName = function() {
				
		loginService.getName().success(
			function(response) {
					$scope.loginName=response.loginName;
			});
		}
	
});
