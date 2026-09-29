package com.influencerportal.ui;

import com.influencerportal.service.AdminService;
import com.influencerportal.service.AuthService;
import com.influencerportal.service.CampaignService;
import com.influencerportal.service.PaymentService;
import com.influencerportal.service.UserService;

/** The services a menu may call. Menus contain no SQL and no business rules. */
public record Services(AuthService auth, UserService users, CampaignService campaigns, PaymentService payments,
                       AdminService admin) {
}
