//商品详细页(控制层)
app.controller('itemController', function($scope, $http) {
	// 数量操作
	$scope.addNum = function(x) {
		$scope.num += x;
		if ($scope.num < 1) {
			$scope.num = 1;
		}
	}

	$scope.specificationItems = {};// 记录用户选择的规格
	// 用户选择规格
	$scope.selectSpecification = function(name, value) {
		$scope.specificationItems[name] = value;
		searchSku();
	}

	// 判断某规格选项是否被选中
	$scope.isSelected = function(name, value) {
		if ($scope.specificationItems[name] == value) {
			return true;
		} else {
			return false;
		}
	}

	// 查询
	searchSku = function() {
		for (var i = 0; i < skuList.length; i++) {
			if (matchObject(skuList[i].spec, $scope.specificationItems)) {
				$scope.sku = skuList[i];
			}
		}
	}

	// 匹配两个对象是否相等
	matchObject = function(map1, map2) {
		for ( var k in map1) {
			if (map1[k] != map2[k]) {
				return false;
			}
		}

		for ( var k in map2) {
			if (map2[k] != map1[k]) {
				return false;
			}
		}

		return true;
	}

	// 加载默认的SKU
	$scope.loadDefaultSku = function() {
		for (var i = 0; i < skuList.length; i++) {
			if (skuList[i].isDefault == '1') {
				$scope.sku = skuList[i];
				break;
			}
		}
		if ($scope.sku == null) {
			$scope.sku = skuList[0];
		}
		$scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
	}

	// 添加商品到购物车
	$scope.addGoodsToCartList = function() {
		
		//将字符串sku.id转换成long类型的sku.id
		var itemId = Number($scope.sku.id.replace(new RegExp(',','gm'),''));
		
		$http.get(
				'http://localhost:9105/cart/addGoodsToCartList.do?itemId='
						+ itemId + '&num=' + $scope.num, {
					'withCredentials' : true
				}).success(function(response) {
			if (response.success) {
				location.href = "http://localhost:9105/cart.html";
			} else {
				alert(response.message);
			}
		});

	}

});