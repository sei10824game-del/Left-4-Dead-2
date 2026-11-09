# 引き継ぎメモ (Left 4 Dead Infected / Forge 1.20.1)

## ビルド
- JDK17必須: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64; ./gradlew --no-daemon build`
- 出力: `build/libs/l4dinfected-1.0.0.jar` / 起動確認: `./gradlew runClient`(要GUI)
- サーバー起動のみ確認済み(MODエラーなし)。実エンティティのsummon・クライアント描画は**未検証**。

## 構成
- `L4D.java` 登録(全17種+弾3種+スポーンエッグ+バイオーム湧き `data/.../biome_modifier/spawns.json`)
- `Hostility.java` 非感染モブ全てが感染者を攻撃
- `entity/Infected.java` 基底: action API(`startAction(id,len)`/`onActionTick`、同期済みで攻撃フレームに判定)、感染者同士は敵対しない
- `entity/CommonInfected.java` コモン+亜種(ceda/mudman/roadcrew/fallen/riot/clown/jimmy)の共通基底
- `entity/Grip.java` 拘束/運搬の共通ヘルパ(Smoker/Charger)、`BloaterBase` (Boomer/Boomette)
- `client/` モデルは `assets/l4dinfected/models/entity/*.json` を `JsonModel` が読込。`InfectedModel`/`CommonModel`/`CrouchModel` がアニメ基底、`Anim` がイージング
- テクスチャは `tools/tex_*.py` で生成(`texlib.py`/`kit_infected.py`)。プレビュー: `tools/preview/`(gitignore)。UVはjson側から自動配置

## 次にやること
1. `runClient`で全17種を確認(クリエイティブタブ「Left 4 Dead 感染者」→スポーンエッグ)。見た目/アニメ崩れ修正
2. 要実機確認: Jockey騎乗表示、Hunter拘束、Smoker舌描画(`smoker_tongue.png`)、Tank岩投げ/ボスバー、Charger運搬/叩きつけ、Witch怒り遷移
3. バランス調整、サウンド(現状バニラ流用)、スポーン重み
- 見た目は全て独自デザイン(原作の外観を写さない)。表示名は汎用名(Gridiron Brute等)。
