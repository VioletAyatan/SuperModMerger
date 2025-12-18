1、这是一个Techland游戏的模组合并工具，使用Antlr4进行文本树解析，可以参考Antlr4解析文件：[src/main/antlr4/TechlandScript.g4](src/main/antlr4/TechlandScript.g4)

2、[mods](mods)目录包含几个测试合并用的mod，我需要的最终效果就是将这几个mod里面的文件全部合并到一个模组中，.pak文件本质上就是个zip压缩包。

3、合并策略为，不重名的文件就简单的放到一个模组文件中就行，对于重名的文件，则需要进行内容对比，scr文件使用Scr解析脚本分析语法树分析，策略如下：
1、你需要能理解scr脚本的格式，对于相同函数块中相同的key对比参数，参数不同则认为冲突，需要记录代码行数，代码内容，标记为冲突，如上述描述一般递归的分析。

你可以参考 [examples](examples) 目录中的scr文件来理解脚本格式。

这里放一个演示的合并效果：

```javascript
//模组1的文件
sub main() {
	Jump("Normal") {
		Height("NormalJumpHeight");
		JumpHoldTime("NormalHoldJumpTime");
		JumpHoldNoInterpolation(false);
		Prepare(0.1);
		BeginAnimation("Jump_Begin");
		LoopAnimation("Jump_Loop");
		AnimationVariants(3);
		MaxSpeed(5.1);
		LookAngles(180, 90, -180, -70);
		LookState("Jump");
		AllowDoubleAndFarJump(false); 
		BlockWhenExhausted(false);
		MoveDirChangeTime(0.6);
		AutoStepHeight("JumpAutoStepHeight");
		ClearPhysics(true);
		BeforeHangTime(0.2);
		EnableCameraAnimations(true);
		ProgressionAction("JumpOnLow", 0.8);
		ExtraGravity(-2.8);
		ExtraGravityDelay(0.4);
		ExtraGravityOnlyWhenFalling(true);
		SpeedPostProcess(0.2, 1.0);
		IsAffectedByLowerJumpOption(true);
		AdvancedParkour() {
			AllowVelocityMod(true);
			MaxSpeed("MoveSprintSpeed");
			ExtraGravity(-8.55);
			ExtraGravityDelay(0.0);
			ExtraGravityOnlyWhenFalling(false);
			Height("@ClearMapping");
			Height("AdvancedParkourNormalJumpHeight");
			JumpHoldTime("@ClearMapping");
			JumpHoldTime("AdvancedParkourNormalHoldJumpTime");
		}
	}
}
//模组2的文件
sub main() {
	Jump("Normal") {
		Height("NormalJumpHeight");
		JumpHoldTime("NormalHoldJumpTime");
		JumpHoldNoInterpolation(false);
		Prepare(0.1);
		BeginAnimation("Jump_Begin");
		LoopAnimation("Jump_Loop");
		AnimationVariants(3);
		MaxSpeed(5.1);
		LookAngles(180, 90, -100, -70); //这一行参数不一致，标记发生了冲突，需要提醒用户使用哪个mod的参数
        Spped(120); //这是模组2多出来的一行函数，但是与模组1没有冲突，需要整合到最终的合并模组中。
		LookState("Jump");
		AllowDoubleAndFarJump(false); 
		BlockWhenExhausted(false);
		MoveDirChangeTime(0.6);
		AutoStepHeight("JumpAutoStepHeight");
		ClearPhysics(true);
		BeforeHangTime(0.2);
		EnableCameraAnimations(true);
		ProgressionAction("JumpOnLow", 0.8);
		ExtraGravity(-2.8);
		ExtraGravityDelay(0.4);
		ExtraGravityOnlyWhenFalling(true);
		SpeedPostProcess(0.2, 1.0);
		IsAffectedByLowerJumpOption(true);
		AdvancedParkour() {
			AllowVelocityMod(true);
			MaxSpeed("MoveSprintSpeed");
			ExtraGravity(-8.55);
			ExtraGravityDelay(0.0);
			ExtraGravityOnlyWhenFalling(false);
			Height("@ClearMapping");
			Height("AdvancedParkourNormalJumpHeight");
			JumpHoldTime("@ClearMapping");
			JumpHoldTime("AdvancedParkourNormalHoldJumpTime");
		}
	}
}

//最终的合并效果
sub main() {
	Jump("Normal") {
		Height("NormalJumpHeight");
		JumpHoldTime("NormalHoldJumpTime");
		JumpHoldNoInterpolation(false);
		Prepare(0.1);
		BeginAnimation("Jump_Begin");
		LoopAnimation("Jump_Loop");
		AnimationVariants(3);
		MaxSpeed(5.1);
		LookAngles(180, 90, -100, -70); //假设用户选择了模组2的参数，使用2的参数替换。
        Spped(120); //整合了多出来的参数
		LookState("Jump");
		AllowDoubleAndFarJump(false); 
		BlockWhenExhausted(false);
		MoveDirChangeTime(0.6);
		AutoStepHeight("JumpAutoStepHeight");
		ClearPhysics(true);
		BeforeHangTime(0.2);
		EnableCameraAnimations(true);
		ProgressionAction("JumpOnLow", 0.8);
		ExtraGravity(-2.8);
		ExtraGravityDelay(0.4);
		ExtraGravityOnlyWhenFalling(true);
		SpeedPostProcess(0.2, 1.0);
		IsAffectedByLowerJumpOption(true);
		AdvancedParkour() {
			AllowVelocityMod(true);
			MaxSpeed("MoveSprintSpeed");
			ExtraGravity(-8.55);
			ExtraGravityDelay(0.0);
			ExtraGravityOnlyWhenFalling(false);
			Height("@ClearMapping");
			Height("AdvancedParkourNormalJumpHeight");
			JumpHoldTime("@ClearMapping");
			JumpHoldTime("AdvancedParkourNormalHoldJumpTime");
		}
	}
}
```

