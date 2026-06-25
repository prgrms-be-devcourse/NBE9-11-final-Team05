package com.back.ovengers.domain.user.repository;

import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.back.ovengers.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> searchUsers(Role role, Status status, boolean includeDeleted, String keyword, Pageable pageable) {

        List<User> content = queryFactory
                .selectFrom(user)
                .where(
                        roleEq(role),
                        statusEq(status),
                        notDeleted(includeDeleted),
                        keywordContains(keyword)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.createdAt.desc())
                .fetch();

        // countQuery 분리 - 페이지네이션 최적화
        // content 쿼리와 달리 count만 필요하므로 orderBy 불필요
        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        roleEq(role),
                        statusEq(status),
                        notDeleted(includeDeleted),
                        keywordContains(keyword)
                )
                .fetchOne();

        // JPA가 자동으로 만들어줬지만 수동으로 해야함
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    // role 필터
    private BooleanExpression roleEq(Role role) {
        return role != null ? user.role.eq(role) : null;
    }

    // status 필터
    private BooleanExpression statusEq(Status status) {
        return status != null ? user.status.eq(status) : null;
    }

    // 탈퇴 회원 필터
    // includeDeleted=false → deletedAt IS NULL 조건 추가
    // includeDeleted=true → 조건 없음 (탈퇴 회원 포함)
    private BooleanExpression notDeleted(boolean includeDeleted) {
        return !includeDeleted ? user.deletedAt.isNull() : null;
    }

    // 키워드 검색 - 이름, 이메일, 닉네임 중 하나라도 포함되면 조회
    // null 또는 빈 문자열이면 조건 무시
    private  BooleanExpression keywordContains(String keyword) {
        if(keyword == null || keyword.isBlank()) {
            return null;
        }
        return user.name.containsIgnoreCase(keyword)
                .or(user.email.containsIgnoreCase(keyword))
                .or(user.nickname.containsIgnoreCase(keyword));
    }
}
