package net.yawn;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

enum NotificationType {
	TOAST,
	TEXT,
	DISABLED
}

public class Yawn implements ClientModInitializer {
	private KeyBinding yawKey;
	private KeyBinding pitchKey;
	private KeyBinding bothKey;
	private KeyBinding clearKey;
	private KeyBinding toggleKey;

	private boolean isYaw = false;
	private boolean isPitch = false;
	private float yaw = 0.0f;
	private float pitch = 0.0f;
	private NotificationType notificationType = NotificationType.TEXT;

	@Override
	public void onInitializeClient() {
		// Register key bindings
		yawKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.yawn.lock_yaw",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_Y,
				"category.yawn"
		));

		pitchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.yawn.lock_pitch",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_P,
				"category.yawn"
		));

		bothKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.yawn.lock_both",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"category.yawn"
		));

		clearKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.yawn.clear",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_C,
				"category.yawn"
		));

		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.yawn.toggle_notification",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				"category.yawn"
		));

		// Listen for key presses
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				handleKeyPresses(client);
			}
		});
	}

	private void handleKeyPresses(MinecraftClient client) {
		while (yawKey.wasPressed()) {
			isYaw = !isYaw;
			yaw = client.player.getYaw();
			showNotification(client, "Yaw " + (isYaw ? "Locked" : "Unlocked"));
		}

		while (pitchKey.wasPressed()) {
			isPitch = !isPitch;
			pitch = client.player.getPitch();
			showNotification(client, "Pitch " + (isPitch ? "Locked" : "Unlocked"));
		}

		while (bothKey.wasPressed()) {
			if (!isYaw && !isPitch) {
				isYaw = true;
				isPitch = true;
				yaw = client.player.getYaw();
				pitch = client.player.getPitch();
				showNotification(client, "Both Locked");
			} else {
				isYaw = false;
				isPitch = false;
				showNotification(client, "Both Unlocked");
			}
		}

		while (clearKey.wasPressed()) {
			isYaw = false;
			isPitch = false;
			showNotification(client, "Locks Cleared");
		}

		while (toggleKey.wasPressed()) {
			notificationType = switch (notificationType) {
				case DISABLED -> NotificationType.TEXT;
				case TEXT -> NotificationType.TOAST;
				case TOAST -> NotificationType.DISABLED;
			};
			showNotification(client, "Notification Type: " + notificationType);
		}

		// Lock the camera angles
		if (isYaw) {
			client.player.setYaw(yaw);
		}
		if (isPitch) {
			client.player.setPitch(pitch);
		}
	}

	private void showNotification(MinecraftClient client, String message) {
		if (client == null || client.player == null) return;
		switch (notificationType) {
			case TEXT -> {
				client.player.sendMessage(Text.literal(message), true);
			}
			case TOAST -> {
				SystemToast toast = new SystemToast(SystemToast.Type.WORLD_BACKUP, Text.literal("Yawn"), Text.literal(message));
				client.getToastManager().add(toast);
			}
			case DISABLED -> {
				// Do nothing
			}
		}
	}
}
