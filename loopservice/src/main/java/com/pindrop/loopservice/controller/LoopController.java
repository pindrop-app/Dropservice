package com.pindrop.loopservice.controller;

import com.pindrop.loopservice.dto.CreateLoopRequest;
import com.pindrop.loopservice.dto.JoinRequestItem;
import com.pindrop.loopservice.models.LoopInviteItem;
import com.pindrop.loopservice.models.LoopItem;
import com.pindrop.loopservice.models.LoopMemberItem;
import com.pindrop.loopservice.service.LoopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/loops")
@RequiredArgsConstructor
public class LoopController {

    private final LoopService loopService;

    @GetMapping("/discover")
    public List<LoopItem> discover() {
        return loopService.discoverLoops();
    }

    @GetMapping("/joined")
    public List<LoopItem> myLoops(@RequestParam String userId) {
        return loopService.getMyLoops(userId);
    }

    @PostMapping("/{loopId}/join")
    public void join(@PathVariable String loopId,
                     @RequestParam String userId) {
        loopService.joinOrRequest(loopId, userId);
    }

    @PostMapping("/{loopId}/requests/{userId}/accept")
    public void acceptRequest(@PathVariable String loopId,
                              @PathVariable String userId) {
        loopService.acceptRequest(loopId, userId);
    }

    @PostMapping("/{loopId}/requests/{userId}/reject")
    public void rejectRequest(@PathVariable String loopId,
                              @PathVariable String userId) {
        loopService.rejectRequest(loopId, userId);
    }


    /* ======================================================
       CREATE LOOP
       ====================================================== */
    @PostMapping
    public ResponseEntity<Void> createLoop(
            @RequestBody CreateLoopRequest request) {

        loopService.createLoop(
                request.getUserId(),
                request.getName(),
                request.getDescription(),
                request.isPrivate()
        );
        return ResponseEntity.ok().build();
    }

    /* ======================================================
       JOIN / LEAVE LOOP
       ====================================================== */


    @PostMapping("/{loopId}/leave")
    public ResponseEntity<Void> leaveLoop(
            @PathVariable String loopId,
            @RequestParam String userId) {

        loopService.leaveLoop(loopId, userId);
        return ResponseEntity.ok().build();
    }

    /* ======================================================
       MEMBERS
       ====================================================== */
    @GetMapping("/{loopId}/members")
    public ResponseEntity<List<LoopMemberItem>> getMembers(
            @PathVariable String loopId) {

        return ResponseEntity.ok(loopService.getMembers(loopId));
    }

    /* ======================================================
       INVITES
       ====================================================== */
    @GetMapping("/invites")
    public ResponseEntity<List<JoinRequestItem>> getMyInvites(
            @RequestParam String userId) {

        return ResponseEntity.ok(loopService.getMyInvites(userId));
    }

    @PostMapping("/{loopId}/invite")
    public ResponseEntity<Void> sendInvite(
            @PathVariable String loopId,
            @RequestParam String senderId,
            @RequestParam String targetUserId) {

        loopService.sendInvite(loopId, senderId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{loopId}/invites/accept")
    public ResponseEntity<Void> acceptInvite(
            @PathVariable String loopId,
            @RequestParam String userId) {

        loopService.acceptInvite(loopId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{loopId}/invites/reject")
    public ResponseEntity<Void> rejectInvite(
            @PathVariable String loopId,
            @RequestParam String email) {

        loopService.rejectInvite(loopId, email);
        return ResponseEntity.ok().build();
    }

    /* ======================================================
       OWNER / ROLE MANAGEMENT
       ====================================================== */
    @PostMapping("/{loopId}/members/{targetUserId}/promote")
    public ResponseEntity<Void> promoteToOwner(
            @PathVariable String loopId,
            @PathVariable String targetUserId,
            @RequestParam String actorUserId) {

        loopService.promoteToOwner(loopId, targetUserId, actorUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{loopId}/members/{targetUserId}/demote")
    public ResponseEntity<Void> demoteOwner(
            @PathVariable String loopId,
            @PathVariable String targetUserId,
            @RequestParam String actorUserId) {

        loopService.demoteOwner(loopId, targetUserId, actorUserId);
        return ResponseEntity.ok().build();
    }
}

