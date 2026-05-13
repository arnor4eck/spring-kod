package com.arnor4eck.springkod.repository;

import com.arnor4eck.springkod.entity.datasitory_member.DatasitoryMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasitoryMemberRepository extends JpaRepository<DatasitoryMember, Long> {
}
