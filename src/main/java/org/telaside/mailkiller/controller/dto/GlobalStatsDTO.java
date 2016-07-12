package org.telaside.mailkiller.controller.dto;

import java.util.ArrayList;
import java.util.List;

import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailAccountStatus;
import org.telaside.mailkiller.domain.EmailKillerUser;

public class GlobalStatsDTO {
	
	public class StatsPerAccount {
		public String accountName;
		public String accountType;
		public List<EmailAccountStatus> stats;
	}
	
	private String principalEmailAddress;
	private List<StatsPerAccount> statsPerAccounts = new ArrayList<StatsPerAccount>();

	public static GlobalStatsDTO mapFromUserAndStats(EmailKillerUser user, List<EmailAccountStatus> emailsPerStatus) {
		GlobalStatsDTO globalStatDTO = new GlobalStatsDTO();
		globalStatDTO.setPrincipalEmailAddress(user.getPrincipalEmailAddress());
		if(!(emailsPerStatus == null || emailsPerStatus.size() == 0)) {
			
		}
		return globalStatDTO;
	}

	public String getPrincipalEmailAddress() {
		return principalEmailAddress;
	}

	public void setPrincipalEmailAddress(String principalEmailAddress) {
		this.principalEmailAddress = principalEmailAddress;
	}

	public List<StatsPerAccount> getStatsPerAccounts() {
		return statsPerAccounts;
	}

	public void setStatsPerAccounts(List<StatsPerAccount> statsPerAccounts) {
		this.statsPerAccounts = statsPerAccounts;
	}

	public void forUser(EmailKillerUser user) {
		setPrincipalEmailAddress(user.getPrincipalEmailAddress());
	}

	public void addAccountStats(EmailAccount account, List<EmailAccountStatus> emailsPerStatus) {
		StatsPerAccount statsPerAccount = new StatsPerAccount();
		statsPerAccount.accountName = account.getLogin();
		statsPerAccount.accountType = account.getAccountType();
		statsPerAccount.stats = emailsPerStatus;
		statsPerAccounts.add(statsPerAccount);
	}
}
