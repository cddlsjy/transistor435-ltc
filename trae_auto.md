# 自动化构建与上传指令

请执行以下步骤，无需询问我确认，自动尝试修复错误：

1. 解压后的项目根目录为当前工作目录。

2. 执行编译命令：`gradlew assembleDebug`
   如果失败，请读取终端错误信息，修改对应源码，然后重新编译。
   直到成功。。

3. 编译成功后，将生成的 APK和源代码上传gitee


4. 所有步骤自动执行，不要中途询问我。
5. 根据您提供的代码和问题描述，应用在启用“自动播放上一次电台”后启动时会出现播放/暂停反复切换的现象。根本原因在于 **`MainFragment` 中对 `CollectionViewModel` 的 LiveData 观察者被重复注册**，导致每次数据更新时自动播放逻辑被多次执行，从而引起播放状态的交替变化。

## 问题分析

### 1. 自动播放逻辑所在位置
在 `MainFragment.kt` 的 `observeCollectionViewModel()` 方法中：
```kotlin
private fun observeCollectionViewModel() {
    collectionViewModel.collectionLiveData.observe(this, Observer<Collection> { updatedCollection ->
        // ... 更新 UI
        if (PreferencesHelper.loadAutoPlayLastStation()) {
            val position = PreferencesHelper.loadLastPlayedStationPosition()
                .coerceIn(0, collection.stations.size - 1)
            mainActivity.onPlayButtonTapped(position)   // 触发播放
        }
    })
}
```

### 2. 观察者注册时机
`observeCollectionViewModel()` 在 `MainFragment.onResume()` 中被调用：
```kotlin
override fun onResume() {
    super.onResume()
    // ...
    observeCollectionViewModel()   // ⚠️ 每次 onResume 都会重新注册
}
```

而 `onResume()` 在以下情况下会被调用：
- Activity 首次启动
- 从其他界面（如设置）返回
- 从后台切回前台

每次调用都会创建一个**新的 Observer 实例**并注册到同一个 `LiveData` 上。由于 Fragment 的生命周期并没有被销毁，这些观察者会堆积，导致 `LiveData` 每次更新值时**所有观察者都会执行其回调**。

### 3. 连锁效应
- `CollectionViewModel` 初始化时会加载一次数据（触发一次观察者回调）。
- 如果随后收到广播 `ACTION_COLLECTION_CHANGED`（例如从自动备份、导入等操作触发），`LiveData` 会再次更新，再次触发所有观察者。
- 多个观察者几乎同时调用 `onPlayButtonTapped(position)`，第一个调用会开始播放，第二个调用（相同 stationPosition 且正在播放）则会执行 `pause()`，造成播放/暂停快速交替。

### 4. 其他潜在因素
- `PreferencesHelper.loadAutoPlayLastStation()` 默认值为 `false`，但用户手动开启后问题复现。
- `PreferencesHelper.loadLastPlayedStationPosition()` 返回的索引是有效的。
- 没有额外的防抖或状态检查，例如播放前判断是否已经处于 `isPlaying` 状态且播放的 station 相同——但这种逻辑只应影响手动操作，自动播放期望总是开始播放，不应触发暂停。

## 解决方案

### 主要修复：确保观察者仅注册一次
将 `observeCollectionViewModel()` 移至 `onCreateView()` 或 `onViewCreated()`，并配合 `viewLifecycleOwner`（若使用 `androidx.fragment.app.Fragment`）。

**推荐修改：**
```kotlin
// 在 MainFragment 中添加标志或利用 lifecycleOwner 特性
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // 只注册一次，使用 viewLifecycleOwner 确保在视图销毁时自动移除
    observeCollectionViewModel()
}

// 修改 observeCollectionViewModel 使用 viewLifecycleOwner
private fun observeCollectionViewModel() {
    collectionViewModel.collectionLiveData.observe(viewLifecycleOwner, Observer { updatedCollection ->
        // ... 原有逻辑
    })
}
```

同时**删除 `onResume()` 中的 `observeCollectionViewModel()` 调用**，以避免重复注册。

### 增强稳定性：在自动播放前增加状态判断
防止因多次回调导致的重复播放/暂停，可在 `BaseMainActivity.onPlayButtonTapped()` 中添加简单的防抖逻辑（例如记录上次请求时间），或者检查当前播放状态是否已经符合预期：

```kotlin
fun onPlayButtonTapped(stationPosition: Int) {
    // 防止对同一 station 连续调用
    if (controller?.isPlaying == true && stationPosition == playerState.stationPosition) {
        // 如果已经在播放，不再重复执行任何操作
        return
    }
    // 后续原有逻辑...
}
```

这样，即使多个观察者同时调用，第二次及以后的调用会因为“已经播放相同的 station”而被直接忽略。

### 补充建议：使用一次性自动播放标志
如果希望自动播放仅在应用冷启动时执行一次（而非每次 collection 更新都执行），可以在 `PreferencesHelper` 中增加一个标志 `autoPlayExecuted`，在第一次自动播放后设为 `true`，并在下次启动时重置。

## 总结
问题根因是 **LiveData 观察者重复注册** 导致自动播放逻辑被多次触发。将观察者注册移到 `onViewCreated` 并配合 `viewLifecycleOwner` 即可解决。同时，可增加播放前状态检查作为防御性措施。修改后，开启“自动播放上一次电台”将仅在应用启动时正常播放一次，不会再出现按钮反复切换的现象。
