package org.mm.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.mm.entity.SecondUserEntity;
import org.mm.entity.Session;

import org.mm.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class SessionService 
{
	@Autowired
	private SessionRepository sessionRepo;
		
	private final int SESSION_LIMIT = 2;
	
	public void generateNewSession(SecondUserEntity secondUser, String refreshToken)
	{
		
		List<Session> userSessions = sessionRepo.findBySecondUser(secondUser);
		
		if(userSessions.size() == SESSION_LIMIT)
		{
			userSessions.sort(Comparator.comparing(Session::getLastUsedAt));
			
			Session leastRecentlyUsedSession = userSessions.getFirst();
			
			sessionRepo.delete(leastRecentlyUsedSession);
		}
		
		Session newSession = new Session();
		
		newSession.setSecondUser(secondUser);
		newSession.setRefreshToken(refreshToken);
		
		sessionRepo.save(newSession);
	}
	
	public void validateSession(String refreshToken)
	{
		Session session = sessionRepo.findByRefreshToken(refreshToken)
					.orElseThrow(() -> new SessionAuthenticationException("Session not found for refreshToken" + refreshToken));
		
		session.setLastUsedAt(LocalDateTime.now());
		sessionRepo.save(session);
	}
	
	public List<Session> getUserDataFromSession(SecondUserEntity secondUser)
	{
		List<Session> userSessions = sessionRepo.findBySecondUser(secondUser);
		return userSessions;
	}
	
}
