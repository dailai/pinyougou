package com.pinyougou.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

/**
 * 认证类
 * @author dailai
 *
 */
public class UserDetailsServiceImpl implements UserDetailsService{

	private SellerService sellerService;
	
	
	
	public SellerService getSellerService() {
		return sellerService;
	}



	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}



	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {	
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		TbSeller seller = sellerService.findOne(username);
		if (seller!=null) {
			if (seller.getStatus().equals("1")) {
				return new User(username, seller.getPassword(), authorities );
			} else {
				System.out.println("审核未通过或者未审核");
				return null;
			}			
		} else {
			System.out.println("没有该公司");
			return null;
		}
		
	}

}
