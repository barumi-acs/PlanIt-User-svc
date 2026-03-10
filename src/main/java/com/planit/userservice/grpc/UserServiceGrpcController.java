package com.planit.userservice.grpc;

import com.planit.grpc.user.CheckFriendshipRequest;
import com.planit.grpc.user.CheckFriendshipResponse;
import com.planit.grpc.user.GetUserNamesRequest;
import com.planit.grpc.user.GetUserNamesResponse;
import com.planit.grpc.user.UserServiceGrpc;
import com.planit.userservice.entity.UserEntity;
import com.planit.userservice.repository.FriendRepository;
import com.planit.userservice.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Schedule Service 등의 외부 서비스에게 유저 정보를 제공하는 gRPC 서버
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserServiceGrpcController extends UserServiceGrpc.UserServiceImplBase {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Override
    public void checkFriendship(CheckFriendshipRequest request, StreamObserver<CheckFriendshipResponse> responseObserver) {
        String userId = request.getUserId();
        String friendUserId = request.getFriendUserId();
        
        log.info("📡 gRPC 친구 확인 요청: {} <-> {}", userId, friendUserId);

        // DB 조회: 수락된 친구 관계가 있는지 확인 (A->B 또는 B->A)
        boolean isFriend = friendRepository.existsAcceptedFriendship(userId, friendUserId);

        CheckFriendshipResponse response = CheckFriendshipResponse.newBuilder()
                .setIsFriend(isFriend)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        
        log.info("✅ gRPC 친구 확인 응답: isFriend={}", isFriend);
    }

    @Override
    public void getUserNames(GetUserNamesRequest request, StreamObserver<GetUserNamesResponse> responseObserver) {
        List<String> userIds = request.getUserIdsList();
        log.info("📡 gRPC 유저 닉네임 조회 요청: {}건", userIds.size());

        List<UserEntity> users = userRepository.findAllById(userIds);
        Map<String, String> userNames = users.stream()
                .collect(Collectors.toMap(UserEntity::getUserId, UserEntity::getNickname));

        GetUserNamesResponse response = GetUserNamesResponse.newBuilder()
                .putAllUserNames(userNames)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        log.info("✅ gRPC 유저 닉네임 조회 응답: {}건 완료", userNames.size());
    }
}
