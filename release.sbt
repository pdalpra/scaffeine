import sbtrelease.ReleaseStateTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)

releaseCrossBuild := true
crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.1")

publishTo := sonatypePublishTo.value
publishMavenStyle := true

TravisCredentials.updateCredentials()

pomExtra in Global := {
    <scm>
      <connection>scm:git:github.com/blemale/scaffeine.git</connection>
      <developerConnection>scm:git:git@github.com:blemale/scaffeine.git</developerConnection>
      <url>github.com/blemale/scaffeine.git</url>
    </scm>
    <developers>
      <developer>
        <id>blemale</id>
        <name>Bastien LEMALE</name>
        <url>https://github.com/blemale</url>
      </developer>
    </developers>
}
