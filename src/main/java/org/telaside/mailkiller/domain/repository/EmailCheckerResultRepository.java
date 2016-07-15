package org.telaside.mailkiller.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.telaside.mailkiller.domain.EmailCheckerResult;

public interface EmailCheckerResultRepository extends CrudRepository<EmailCheckerResult, Long> {

}
