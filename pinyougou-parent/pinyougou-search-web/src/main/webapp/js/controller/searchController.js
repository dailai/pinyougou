app.controller('searchController',function($scope,searchService){	
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo);
		searchService.search( $scope.searchMap ).success(
			function(response){						
				$scope.resultMap=response;//搜索返回的结果
				$scope.resultMap.pageLabel=[];
				if(response.currentPage>2){
					for(var i=response.currentPage-2;i<=response.currentPage+2&&i<=response.totalPages;i++){		
						$scope.resultMap.pageLabel.push(i);
					}
				}else{
					for(var i=1;i<response.currentPage+5&&i<=response.totalPages;i++){		
						$scope.resultMap.pageLabel.push(i);
					}
				}
			}
		);	
	}	
	
	//搜索条件
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sort':'ASC'};
	/**
	 * 选中的搜索条件加入searchMap
	 */
	$scope.addSearchItem=function(key,value){
		if (key=='category'|| key=='brand'|| key=='price'){
			$scope.searchMap[key]=value;
		}else {
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//增加搜索条件查询
	}
	
	/**
	 * 从搜索条件中删除某个条件
	 */
	$scope.removeSearchItem=function(key){
		if (key=='category'|| key=='brand'|| key=='price') {
			$scope.searchMap[key]='';
		} else {
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//删除搜索条件查询
	}
	
	
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		//页码验证
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}
	
	/**
	 * 判断是否当前选中页
	 */
	$scope.isPageSelected=function(number){
		if (number==$scope.searchMap.pageNo) {
			return true;
		} else {
			return false;
		}
	}
	
	//设置排序规则
	$scope.setSort=function(value){
		$scope.searchMap.sort=value;
		$scope.search();
	}

	//判断价格排序是否选中
	$scope.isPriceSelected=function(value){
		if(value==$scope.searchMap.sort){
			return true;
		}else{
			return false;
		}
	}
	
});
