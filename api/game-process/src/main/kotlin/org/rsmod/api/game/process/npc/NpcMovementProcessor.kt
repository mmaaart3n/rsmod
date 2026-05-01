package org.rsmod.api.game.process.npc

import jakarta.inject.Inject
import org.rsmod.api.npc.events.NpcMovementEvent
import org.rsmod.api.route.StepFactory
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Npc
import org.rsmod.game.movement.MoveSpeed
import org.rsmod.game.movement.RouteRequest
import org.rsmod.game.movement.RouteRequestCoord
import org.rsmod.game.movement.RouteRequestLoc
import org.rsmod.game.movement.RouteRequestPathingEntity
import org.rsmod.map.CoordGrid
import org.rsmod.routefinder.RouteCoordinates
import org.rsmod.routefinder.RouteFinding
import org.rsmod.routefinder.collision.CollisionFlagMap
import org.rsmod.routefinder.collision.CollisionStrategy

public class NpcMovementProcessor
@Inject
constructor(
    private val collision: CollisionFlagMap,
    private val stepFactory: StepFactory,
    private val eventBus: EventBus,
) {
    public fun process(npc: Npc) {
        if (npc.pendingForcedWalkDest != null) {
            applyPendingForcedWalk(npc)
            npc.resetTempSpeed()
            return
        }
        if (npc.isControlLocked) {
            npc.routeRequest = null
            npc.abortRoute()
            npc.resetTempSpeed()
            return
        }
        npc.routeRequest?.let { consumeRequest(npc, it) }
        npc.routeRequest = null
        npc.processMovement()
        npc.resetTempSpeed()
    }

    /**
     * Ignores any [Npc.routeRequest] for this cycle (e.g. combat chase) so a one-tile knockback is
     * not overwritten by [org.rsmod.api.npc.interact.AiPlayerInteractions.interactAp] in
     * [org.rsmod.api.game.process.npc.NpcModeProcessor], which runs before movement.
     */
    private fun applyPendingForcedWalk(npc: Npc) {
        val waypoint = npc.pendingForcedWalkDest ?: return
        npc.pendingForcedWalkDest = null
        npc.routeRequest = null

        val collisionStrat =
            npc.collisionStrategy
                ?: run {
                    npc.abortRoute()
                    return
                }

        npc.cachedMoveSpeed = MoveSpeed.Walk
        npc.moveSpeed = MoveSpeed.Walk

        npc.abortRoute()
        npc.routeDestination.add(waypoint)

        npc.processWalkTrigger()
        npc.move(steps = 1, collision = collisionStrat)
        if (npc.pendingStepCount == 0) {
            npc.routeDestination.clear()
        }
    }

    public fun consumeRequest(npc: Npc, request: RouteRequest) {
        npc.routeTo(request)
    }

    private fun Npc.routeTo(request: RouteRequest) {
        val dest = request.destination(this)
        cachedMoveSpeed = tempMoveSpeed ?: defaultMoveSpeed
        moveSpeed = cachedMoveSpeed
        routeDestination.clear()
        routeDestination.add(dest)
    }

    private fun Npc.processMovement() {
        val collision = collisionStrategy
        if (collision != null) {
            processMoveSpeed(collision)
        } else if (routeDestination.isNotEmpty()) {
            resetMovement()
        }
    }

    private fun Npc.processMoveSpeed(collision: CollisionStrategy) {
        if (routeDestination.isEmpty()) {
            return
        }
        processWalkTrigger()

        val completeCrawlStep = moveSpeed == MoveSpeed.Crawl && !hasMovedPreviousCycle
        val steps = if (completeCrawlStep) 1 else moveSpeed.steps
        move(steps, collision)
    }

    private fun Npc.move(steps: Int, collision: CollisionStrategy) {
        val destination = routeDestination
        val waypoint = destination.peekFirst() ?: return
        val start = coords
        var current = start
        var target = waypoint
        var stepCount = 0
        removeBlockWalkCollision(current)
        for (i in 0 until steps) {
            if (current == target) {
                target = destination.pollFirst() ?: break
                if (current == target) {
                    target = destination.peekFirst() ?: break
                }
            }

            val step = stepFactory.validated(this, current, target, collision)
            if (step == CoordGrid.NULL) {
                break
            }
            // Important to set this before `current` is assigned for this iteration. This serves
            // as a way to track the intermediate coord when running.
            lastProcessedCoord = current
            current = step
            stepCount++
        }
        if (current == target) {
            // If last step in on waypoint destination, remove it from the queue.
            destination.pollFirst()
        }
        addBlockWalkCollision(current)
        updateMovementClock(current, start)
        pendingStepCount = stepCount
        coords = current
    }

    private fun Npc.addBlockWalkCollision(coords: CoordGrid) {
        addBlockWalkCollision(collision, coords)
    }

    private fun Npc.removeBlockWalkCollision(coords: CoordGrid) {
        removeBlockWalkCollision(collision, coords)
    }

    private fun Npc.resetTempSpeed() {
        if (routeDestination.isEmpty()) {
            tempMoveSpeed = null
        }
    }

    private fun Npc.updateMovementClock(current: CoordGrid, previous: CoordGrid) {
        if (current != previous) {
            lastMovement = currentMapClock
        }
    }

    private fun RouteRequest.destination(source: Npc): CoordGrid =
        when (this) {
            is RouteRequestCoord -> destination
            is RouteRequestPathingEntity ->
                RouteFinding.naiveDestination(
                        sourceX = source.coords.x,
                        sourceZ = source.coords.z,
                        sourceWidth = source.size,
                        sourceLength = source.size,
                        targetX = destination.coords.x,
                        targetZ = destination.coords.z,
                        targetWidth = destination.size,
                        targetLength = destination.size,
                    )
                    .toCoordGrid()
            is RouteRequestLoc -> destination
        }

    private fun Npc.processWalkTrigger() {
        val trigger = walkTrigger ?: return
        clearWalkTrigger()
        val event = NpcMovementEvent.WalkTrigger(this, trigger)
        eventBus.publish(event)
    }

    private companion object {
        private fun RouteCoordinates.toCoordGrid() = CoordGrid(x, z, level)
    }
}
